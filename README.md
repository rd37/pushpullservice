pushpullservice
===============
README

R. Desmarais Dec 8, 2012 - thanks Ian Gable for 'well just build it then'

This tool is for a command simple copy of files between systems over the web.  For example if I'm working on two systems 
and I wish to move a file from one to the other I generally do the following depending on how well my memory
is working that day :

1. scp the file i.e.    : scp ~/mydocs/doc.pdf user@machineB.com:/home/user/somedocs/. 
   -requires some memory on my part such as directory structures of remote machine and username/passwords
   -but this is the safest way to transfer files in my mind.
2. use my google drive  : open a browser and upload to my google drive then on remote machine open a browser and copy

3. use my email : open a browser and mail it myself or if old school use 'pine'

4. copy to usb stick: open some native explorer and move the files over

5. use dropbox: which becomes tedious and over bloated quickly

I built this tool for myself to simplify file transfer of simple non secure documents
In its simplest form on machine A I run ./push ~/mydocs/doc.pdf  - the tool takes over pushes to a remote server, 
then on machine B I run ./pop  and the last file pushed to the server if copied locally.

Issues/Points
- there is clearly potential for misuse, so I recommend you set up you own remote server which you have exclusive
access to rather than using the current public available server which everyone can use.
- once file is popped off server, it is gone! which I likey so no web content footprint

                        
------- Client Installation Only ---------
-------INSTALL-------
To have server create a new key representing a push/pop queue for you. The new key should 
be created in a config.cfg file in the installation directory.  This file is read when executing 
push pop operations so the client tools (pop.sh / push.sh) know which queue on the server to use.

ensure install.sh is exectuable: chmod +x install.sh 

then

execute: sudo ./install.sh -createkey

or

if you already have a key (the key is just a text string). The string will be written to the config.cfg file
execute: sudo ./install.sh -usekey [key]

*note the installation will create the pop.sh and pull.sh scripts and create links in /opt/local/bin so the scripts
can be executed from any directory without the pathname.  If you do not have sudo, you can still install the tool, but 
you will either need to setup your $path variable or use full pathname to execute pop.sh and push.sh!

-------PUSH-----------
To push a file to the server.  The push.sh tool will first read the config.cfg file to get the key associated
with a push/pop queue on the server.  This tool uses 'curl' to push the file using http PUT method. 

execute: ./push [filelocation/filename]



-------POP------------
To pop a file from the server to your local system.  The pop.sh tool will first read the config.cfg file to get the
key associated with a push/pop queue on the server.  This tool uses 'wget' to pop the file off the server.

this will pop the file off the server and save it as file.out
execute: ./pop

or

this will name the file you popped off the server
execute: ./pop mypoppedfile.whatever
