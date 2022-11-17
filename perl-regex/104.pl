#!/usr/bin/perl
#use strict;
#use warnings;

while (<>) {
	if (/z.{3}z/) {
		print $_;
	}
}
