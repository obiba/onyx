#!/bin/bash
if [ ! -e keystore.jks ]; then 
  ant makekey;
fi;
ant/bin/ant war
