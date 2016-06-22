#!/bin/bash

TDIR=`mktemp -d`

MYDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

pushd $MYDIR/.. > /dev/null

DATE=`date +%Y.%m.%d`
GITHASH=`git log --pretty=format:'%h' -n 1` 

if [ $? -ne 0 ]; then
    VERSION="$DATE"
else
    VERSION="$DATE""-$GITHASH"
fi

ARCHIVENAME="OSTRAJavaCompiled-$VERSION"


mvn clean
mvn install

echo
echo "-------------------------------------------------------"
echo "Creating compiled packages"
echo "-------------------------------------------------------"
echo "Name: $ARCHIVENAME"
echo "Directory: $MYDIR"
echo


mkdir $TDIR/$ARCHIVENAME
mkdir $TDIR/$ARCHIVENAME/target

cp target/*.jar             $TDIR/$ARCHIVENAME/target
cp -r ostrajava_lib         $TDIR/$ARCHIVENAME/
cp -r examples              $TDIR/$ARCHIVENAME/
cp ostrajava                $TDIR/$ARCHIVENAME/
cp ostrajavac               $TDIR/$ARCHIVENAME/
cp README.md                $TDIR/$ARCHIVENAME/

chmod a+x $TDIR/$ARCHIVENAME/ostrajava
chmod a+x $TDIR/$ARCHIVENAME/ostrajavac


# create tar package
tar -zc --file=build/$ARCHIVENAME.tgz --directory=$TDIR $ARCHIVENAME


# remove zip file if exists
[ -f "$MYDIR/$ARCHIVENAME.zip" ] && rm $MYDIR/$ARCHIVENAME.zip

# create zip package
pushd $TDIR > /dev/null
zip -rq $MYDIR/$ARCHIVENAME.zip $ARCHIVENAME
popd > /dev/null

popd > /dev/null


rm -rf $TDIR
