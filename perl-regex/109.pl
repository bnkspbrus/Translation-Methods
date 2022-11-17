#!/usr/bin/perl
#use strict;
#use warnings;

while (<>) {
	if (/^(\S|\S.*\S)?$/) {
		print $_;
	}
}
