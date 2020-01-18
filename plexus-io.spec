Name:           plexus-io
Version:        2.0.5
Release:        9%{?dist}
Summary:        Plexus IO Components

Group:          Development/Libraries
License:        ASL 2.0
URL:            http://plexus.codehaus.org/plexus-components/plexus-io
Source0:        https://github.com/sonatype/plexus-io/tarball/plexus-io-%{version}#/%{name}-%{version}.tar.gz
Source1:        http://www.apache.org/licenses/LICENSE-2.0.txt
BuildArch: noarch

BuildRequires: java-devel >= 1:1.6.0
BuildRequires: jpackage-utils

BuildRequires: plexus-utils
BuildRequires: plexus-containers-container-default
BuildRequires: plexus-components-pom
BuildRequires: maven-local
BuildRequires: maven-compiler-plugin
BuildRequires: maven-enforcer-plugin
BuildRequires: maven-jar-plugin
BuildRequires: maven-install-plugin
BuildRequires: maven-javadoc-plugin
BuildRequires: maven-resources-plugin
BuildRequires: maven-surefire-plugin
BuildRequires: maven-surefire-provider-junit
BuildRequires: maven-doxia-sitetools
BuildRequires: mvn(org.apache.maven.plugins:maven-enforcer-plugin)

%description
Plexus IO is a set of plexus components, which are designed for use
in I/O operations.

%package javadoc
Group:          Documentation
Summary:        Javadoc for %{name}

%description javadoc
API documentation for %{name}.


%prep
%setup -q -n sonatype-plexus-io-1a0010b
cp %{SOURCE1} .

%build
export XMVN_COMPILER_SOURCE="1.5"
%mvn_file  : plexus/io
%mvn_build

%install
%mvn_install

%files -f .mfiles
%doc NOTICE.txt LICENSE-2.0.txt

%files javadoc -f .mfiles-javadoc
%doc NOTICE.txt LICENSE-2.0.txt


%changelog
* Fri Dec 27 2013 Daniel Mach <dmach@redhat.com> - 2.0.5-9
- Mass rebuild 2013-12-27

* Fri Jun 28 2013 Mikolaj Izdebski <mizdebsk@redhat.com> - 2.0.5-8
- Rebuild to regenerate API documentation
- Resolves: CVE-2013-1571

* Wed Jun 12 2013 Stanislav Ochotnicky <sochotnicky@redhat.com> - 2.0.5-7
- Add ASL 2.0 license text to rpms

* Thu Feb 14 2013 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 2.0.5-6
- Rebuilt for https://fedoraproject.org/wiki/Fedora_19_Mass_Rebuild

* Wed Feb 06 2013 Java SIG <java-devel@lists.fedoraproject.org> - 2.0.5-5
- Update for https://fedoraproject.org/wiki/Fedora_19_Maven_Rebuild
- Replace maven BuildRequires with maven-local

* Thu Jan 17 2013 Michal Srb <msrb@redhat.com> - 2.0.5-4
- Build with xmvn

* Thu Nov 22 2012 Jaromir Capik <jcapik@redhat.com> - 2.0.5-3
- Migration to plexus-containers-container-default

* Tue Nov 13 2012 Stanislav Ochotnicky <sochotnicky@redhat.com> - 2.0.5-2
- Use ordinary URL for Source0
- Make sure we use 1.5 source/target
- Add enforcer plugin to BR

* Wed Oct 10 2012 Alexander Kurtakov <akurtako@redhat.com> 2.0.5-1
- Update to upstream 2.0.5 release.

* Sat Jul 21 2012 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 2.0.4-2
- Rebuilt for https://fedoraproject.org/wiki/Fedora_18_Mass_Rebuild

* Wed Apr 18 2012 Alexander Kurtakov <akurtako@redhat.com> 2.0.4-1
- Update to latest upstream.

* Thu Feb 02 2012 Tomas Radej <tradej@redhat.com> - 2.0.2-1
- Updated to upstream version

* Sat Jan 14 2012 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 2.0.1-2
- Rebuilt for https://fedoraproject.org/wiki/Fedora_17_Mass_Rebuild

* Thu Sep 8 2011 Alexander Kurtakov <akurtako@redhat.com> 2.0.1-1
- Update to 2.0.1 upstream release.

* Wed Jul 27 2011 Stanislav Ochotnicky <sochotnicky@redhat.com> - 1.0.1-2
- Use add_maven_depmap macro

* Wed Jul 27 2011 Jaromir Capik <jcapik@redhat.com> - 1.0.1-2
- Removal of plexus-maven-plugin dependency (not needed)
- Minor spec file changes according to the latest guidelines

* Tue May 17 2011 Alexander Kurtakov <akurtako@redhat.com> 1.0.1-1
- Update to upstream 1.0.1.
- Adapt to current guidelines.

* Wed Feb 09 2011 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 1.0-0.3.a5
- Rebuilt for https://fedoraproject.org/wiki/Fedora_15_Mass_Rebuild

* Wed Dec 23 2009 Alexander Kurtakov <akurtako@redhat.com> 1.0-0.2.a5
- Fix review comments.

* Wed Dec 23 2009 Alexander Kurtakov <akurtako@redhat.com> 1.0-0.1.a5.1
- Initial package
