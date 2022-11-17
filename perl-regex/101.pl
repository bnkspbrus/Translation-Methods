#!/usr/bin/perl
#use strict;
#use warnings;

use strict;
use warnings;
while (<>) {
	if (/^.*cat.*cat.*$/) {
		print $_;
	}
}
