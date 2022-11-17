#!/usr/bin/perl
#use strict;
#use warnings;

while (<>) {
	if (/\b(0|[1-9]\d*)\b/) {
		print $_;
	}
}
