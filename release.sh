# Upload javadoc, sign jars and create the jar to upload to sonatype and the zip to upload to google code
# It will also sweep the floor, if asked nicely.

gradle clean build uploadJavadoc uploadArchives
mkdir build/upload
cd build/upload/
cp ../libs/*.jar .
cp ../poms/pom-default.xml pom.xml
echo "THE PASSWORD WILL BE ASKED SEVERAL TIMES! maybe it's  worth to create a new pgp without password after all..."
for i in *.jar pom.xml; do gpg -ab $i; done
jar -cvf ../distributions/sonatype-upload.jar *
cd ../..
echo Now upload ../distributions/*.zip to google code and ../distributions/sonatype-upload to sonatype