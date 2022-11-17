#!/usr/bin/perl
#use strict;
#use warnings;

while (<>) {
	if (/[xyz].{5,17}[xyz]/) {
		print $_;
	}
}
