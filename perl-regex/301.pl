#!/usr/bin/perl
use strict;
use warnings;

my $blank = "^\\s*\$";
my $prevBlank = 1;
my $first = 1;
open(my $in, "<", "input.txt");
open(my $out, ">", "output.txt");
while (<$in>) {
    my $curBlank = $_ =~ /$blank/;
    unless ($curBlank) {
        if ($prevBlank) {
            unless ($first) {
                print $out "\n\n";
            }
        }
        else {
            print $out "\n"
        }
        $_ =~ s/^\s*//;
        $_ =~ s/\s*$//;
        $_ =~ s/   */ /g;
        print $out $_;
        $first = 0;
    }
    $prevBlank = $curBlank;
}
