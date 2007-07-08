#!/bin/sh
SVN=`which svn`
BASE="https://jdtaus.svn.sourceforge.net/svnroot/jdtaus/jdtaus-banking"

$SVN diff $BASE/trunk $BASE/branches/jdtaus-banking-1.x

$SVN diff $BASE/jdtaus-banking-api/trunk \
        $BASE/jdtaus-banking-api/branches/jdtaus-banking-api-1.x

$SVN diff $BASE/jdtaus-banking-charsets/trunk \
        $BASE/jdtaus-banking-charsets/branches/jdtaus-banking-charsets-1.x

$SVN diff $BASE/jdtaus-banking-it/trunk \
        $BASE/jdtaus-banking-it/branches/jdtaus-banking-it-1.x

$SVN diff $BASE/jdtaus-banking-ri-bankleitzahlenverzeichnis/trunk \
        $BASE/jdtaus-banking-ri-bankleitzahlenverzeichnis/branches/jdtaus-banking-ri-bankleitzahlenverzeichnis-1.x

$SVN diff $BASE/jdtaus-banking-ri-currencydirectory/trunk \
        $BASE/jdtaus-banking-ri-currencydirectory/branches/jdtaus-banking-ri-currencydirectory-1.x

$SVN diff $BASE/jdtaus-banking-ri-dtaus/trunk \
        $BASE/jdtaus-banking-ri-dtaus/branches/jdtaus-banking-ri-dtaus-1.x

$SVN diff $BASE/jdtaus-banking-ri-textschluesselverzeichnis/trunk \
        $BASE/jdtaus-banking-ri-textschluesselverzeichnis/branches/jdtaus-banking-ri-textschluesselverzeichnis-1.x

$SVN diff $BASE/jdtaus-banking-spi/trunk \
        $BASE/jdtaus-banking-spi/branches/jdtaus-banking-spi-1.x

$SVN diff $BASE/jdtaus-banking-utilities/trunk \
        $BASE/jdtaus-banking-utilities/branches/jdtaus-banking-utilities-1.x
