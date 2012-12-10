#!/bin/bash

#check to ensure wget and curl is installed
address="http://yakit.ca";
#echo "$0 $1 ";
#if -createkey option then comm with server to generate a key and write config file
#if -usekey key then write config file with that key

if [ $1 == "-h" ] 
then
	echo "usage::";
	echo "install.sh -createkey";
	echo "install.sh -usekey [key]";
fi

if [ $1 == "-usekey" ]
then
	echo "generating new config file with key $2";
	echo -n "$2" > config.cfg
fi


if [ $1 == "-createkey" ]
then
    if  [ -a config.cfg ]
    then
    	echo "config file already exists do you wish to create new key?";
    	echo "a new key will push your documents to a new fifo queue at the server";
    	echo "do you wish to create a new key? [Y,n]?";
    	read ans;
    	if [ $ans == "Y" ]
    	then
    	    echo "success generating new key";
    	    wget -O config.cfg "$address/YakitsPushPull/YakitPushPull?operation=createnewkey";
    	fi
    else
 		wget -O config.cfg "$address/YakitsPushPull/YakitPushPull?operation=createnewkey";
 		echo "success generating new key on server";
 	fi
fi

echo "generate pop.sh and push.sh scripts";

if [ -a push.sh ]
then
   rm push.sh;
fi

touch push.sh;

echo '#!/bin/bash' >> push.sh;
echo 'address="http://yakit.ca"' >> push.sh;
dir=`pwd`;
echo -n 'key=`cat ' >> push.sh;
echo -n $dir >> push.sh;
echo '/config.cfg`;' >> push.sh
echo 'echo "$key";' >> push.sh;

echo 'if [ -e $1 ]' >> push.sh;
echo 'then' >> push.sh;
echo 'curl -X PUT --data-binary "@$1" --header "Content-Type: text/plain" --header "FileName:$1" --header "AppKey:$key" "$address/YakitsPushPull/YakitPushPull";' >> push.sh;
echo 'fi' >> push.sh;

chmod +x push.sh;
ln -F -f  push.sh /usr/local/bin/push.sh;

if [ -a pop.sh ]
then
   rm pop.sh;
fi

touch pop.sh

echo '#!/bin/bash' >> pop.sh;
echo 'address="http://yakit.ca"' >> pop.sh;
dir=`pwd`;
echo -n 'key=`cat ' >> pop.sh;
echo -n $dir >> pop.sh;
echo '/config.cfg`;' >> pop.sh
echo 'echo "$key";' >> pop.sh;

echo 'if [ -e $1 ]' >> pop.sh;
echo 'then' >> pop.sh;
echo '   wget -O file.out  "$address/YakitsPushPull/YakitPushPull?operation=getfile&appkey=$key" ' >> pop.sh;
echo 'else' >> pop.sh;
echo '   wget -O "$1" "$address/YakitsPushPull/YakitPushPull?operation=getfile&appkey=$key" ' >> pop.sh;
echo 'fi' >> pop.sh;

chmod +x pop.sh
ln -F -f  pop.sh /usr/local/bin/pop.sh;