#!/usr/bin/perl
#use strict;
#use warnings;

my $good = "a.*?a";
while (<>) {
	s/($good)($good)($good)/bad/g;
	print;
}
