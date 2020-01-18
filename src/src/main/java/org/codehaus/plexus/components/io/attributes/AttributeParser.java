package org.codehaus.plexus.components.io.attributes;

/*
 * Copyright 2011 The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
* @author Kristian Rosenvold
*/
abstract class AttributeParser
    implements StreamConsumer
{
    protected static final Pattern LINE_SPLITTER = Pattern.compile( "\\s+" );
    protected static final int[] LS_LAST_DATE_PART_INDICES = { 7, 7, 6, 7, 7 };

    protected final StreamConsumer delegate;

    protected final Map<String, PlexusIoResourceAttributes>
        attributesByPath = new LinkedHashMap<String, PlexusIoResourceAttributes>();


    private final Logger logger;

    private boolean nextIsPathPrefix = false;

    private String pathPrefix = "";

    private final SimpleDateFormat[] LS_DATE_FORMATS;

    public AttributeParser( StreamConsumer delegate, Logger logger )
    {
        this.delegate = delegate;
        this.logger = logger;
        LS_DATE_FORMATS =
            new SimpleDateFormat[]{ new SimpleDateFormat( "MMM dd yyyy" ), new SimpleDateFormat( "MMM dd HH:mm" ),
                new SimpleDateFormat( "yyyy-MM-dd HH:mm" ),
                // month-day order is reversed for most non-US locales on MacOSX and FreeBSD
                new SimpleDateFormat( "dd MMM HH:mm" ),
                new SimpleDateFormat( "dd MMM yyyy" )
            };
    }

    public void consumeLine( String line )
    {
        if ( PlexusIoResourceAttributeUtils.totalLinePattern.matcher( line ).matches() )
        {
            // skip it.
        }
        else if ( line.trim().length() == 0 )
        {
            nextIsPathPrefix = true;

            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Anticipating path prefix in next line" );
            }
        }
        else if ( nextIsPathPrefix )
        {
            if ( !line.endsWith( ":" ) )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Path prefix not found. Checking next line." );
                }
            }
            else
            {
                nextIsPathPrefix = false;
                pathPrefix = line.substring( 0, line.length() - 1 );

                if ( !pathPrefix.endsWith( "/" ) )
                {
                    pathPrefix += "/";
                }

                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Set path prefix to: " + pathPrefix );
                }
            }
        }
        else
        {
            String[] parts = LINE_SPLITTER.split( line );
            int lastDatePart = verifyParsability( line, parts, logger );

            if ( lastDatePart > 0 )
            {
                int idx = line.indexOf( parts[lastDatePart] ) + parts[lastDatePart].length() + 1;

                String path = pathPrefix + line.substring( idx );
                while ( path.length() > 0 && Character.isWhitespace( path.charAt( 0 ) ) )
                {
                    path = path.substring( 1 );
                }

                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "path: '" + path + "'" );
                    logger.debug( "mode: '" + parts[0] + "'" );
                    logger.debug( "uid: '" + parts[2] );
                    logger.debug( "gid: '" + parts[3] );
                }

                FileAttributes attributes;
                synchronized ( attributesByPath )
                {
                    attributes = new FileAttributes();
                    attributes.setLsModeline( parts[0] );
                    attributesByPath.put( path, attributes );
                    processAttributes( attributes, parts);
                }
            }
        }

        delegate.consumeLine( line );
    }

    protected abstract void processAttributes( FileAttributes attributes, String[] parts );

    public Map<String, PlexusIoResourceAttributes> getAttributesByPath()
    {
        return attributesByPath;
    }

    private int verifyParsability( String line, String[] parts, Logger logger )
    {
        if ( parts.length > 7 )
        {
            String dateCandidate = parts[5] + " " + parts[6] + " " + parts[7];
            for ( int i = 0; i < LS_DATE_FORMATS.length; i++ )
            {
                try
                {
                    LS_DATE_FORMATS[i].parse( dateCandidate );
                    return LS_LAST_DATE_PART_INDICES[i];
                }
                catch ( ParseException e )
                {
                    if ( logger.isDebugEnabled() )
                    {
                        logger.debug( "Failed to parse date: '" + dateCandidate + "' using format: "
                                          + LS_DATE_FORMATS[i].toPattern(), e );
                    }
                }
            }
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Unparseable line: '" + line
                              + "'\nReason: unrecognized date format; ambiguous start-index for path in listing." );
        }

        return -1;
    }

    static class NumericUserIDAttributeParser
        extends AttributeParser
    {
        NumericUserIDAttributeParser( StreamConsumer delegate, Logger logger )
        {
            super( delegate, logger );
        }

        @Override
        protected void processAttributes( FileAttributes attributes, String[] parts )
        {
            attributes.setUserId( (int) Long.parseLong( parts[2] ) );
            attributes.setGroupId( (int) Long.parseLong( parts[3] ) );

        }
    }

    static class SymbolicUserIDAttributeParser
        extends AttributeParser
    {
        SymbolicUserIDAttributeParser( StreamConsumer delegate, Logger logger )
        {
            super( delegate, logger );
        }

        @Override
        protected void processAttributes( FileAttributes attributes, String[] parts )
        {
            attributes.setUserName( parts[2] );
            attributes.setGroupName( parts[3] );
        }

        public Map<String, PlexusIoResourceAttributes> merge( NumericUserIDAttributeParser otherParser )
        {
            final Map<String, PlexusIoResourceAttributes> attributes = getAttributesByPath();
            if ( otherParser == null )
            {
                return attributes;
            }
            final Map<String, PlexusIoResourceAttributes> result  = new HashMap<String, PlexusIoResourceAttributes>(  );

            final Map<String, PlexusIoResourceAttributes> otherAttributes = otherParser.getAttributesByPath();
            PlexusIoResourceAttributes thisAttribute, otherAttribute;

            Set<String> allKeys = new HashSet<String>(attributes.keySet());
            allKeys.addAll( otherAttributes.keySet() );
            for ( String key : allKeys )
            {
                thisAttribute = attributes.get( key );
                otherAttribute = otherAttributes.get( key );
                if ( thisAttribute == null )
                { // Slight workaround because symbolic parsing is failure prone
                    thisAttribute = otherAttribute;
                }
                if ( thisAttribute != null && otherAttribute != null )
                {
                    thisAttribute.setUserId( otherAttribute.getUserId() );
                    thisAttribute.setGroupId( otherAttribute.getGroupId() );
                }
                result.put( key, thisAttribute);
            }
            return result;
        }
    }
}
