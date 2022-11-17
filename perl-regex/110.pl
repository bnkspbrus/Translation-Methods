#!/usr/bin/perl
#use strict;
#use warnings;

while (<>) {
	if (/\b(\w+)\g1\b/) {
		print $_;
	}
}
