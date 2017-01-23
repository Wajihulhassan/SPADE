#!/usr/bin/bash

./bin/spade stop
 /bin/rm ./log/* 
sudo /bin/rm -f /var/log/audit/audit.log 
sudo service auditd restart 
/bin/rm -f /tmp/provenance.dot 
/bin/rm -f /tmp/audit.log 
./bin/spade start
