
#!usr/bin/perl

# This script takes a lot of constraint images and makes one giant report image out of them.
#
# To run this script, you need to:
# 1) Install ImageMagick on your machine.
# 2) Install PerlMagick on your machine (easiest to do this at the same time as ImageMagick, since it comes with it).
# 3) Have run the Java program
#     test/functional/ladder/recognition/constraint/reporting/ConstraintConfidenceReport.java
#
# Joshua Johnston, Jan 9, 2009

########################

# Perl lib for the ImageMagick routines.
use Image::Magick;

# List (array) routines, like shuffling an array
use List::Util qw(shuffle);

# Some math routines
use POSIX qw(ceil);

########################

# Where are all the individual images stored at? Give the top-level directory
$imageDirectory = "/Users/jbjohns/Desktop/constraintReport";

# How many images from each grouping do you want? Images are grouped by confidence values, for instance into 10s. This
# means there will be a subdirectory for values of 10, like 50. The images in the subdirectory 50 will be for
# constraints that have confidence values in the range 40--50. We'll pick the following number of images at random from
# each group.
$numImagesFromEachGroup = 20;
$numImageRows           = 2;
$numImageCols           = ceil( $numImagesFromEachGroup / $numImageRows );

# how to layout the grouping images in the large constraint image.
$constraintImageTile = "1x";

# Once we make the huge image report, where do you want it saved at? Give the filename.
#$reportDestinationFile="$imageDirectory/report.png";

# size of the individual images that we're stitching together.
$imageSize = 200;

########################

print "Reading constraint directories from ", $imageDirectory, "\n\n";

@constraintDirectories = &ReadValidDirectories($imageDirectory);

$reportImage = Image::Magick->new;

# loop over each constraint
foreach $constraintDir (@constraintDirectories) {
	$thisConstraintDir = $imageDirectory . '/' . $constraintDir;

	# read the contents of this directory
	@groupingDirectories = &ReadValidDirectories($thisConstraintDir);

	# sort the grouping directories numerically
	@groupingDirectories = sort { $a <=> $b } @groupingDirectories;

	# image magick object for this constraint
	$constraintImage = Image::Magick->new;

	# loop over each grouping dir for this constraint
	foreach $groupingDir (@groupingDirectories) {

		# absolute path for this grouping directory
		$thisGroupingDir = $thisConstraintDir . '/' . $groupingDir;

		# image magick object for this grouping
		$groupingImage = Image::Magick->new;

		# read the contents of this grouping directory
		print "\tPicking some random images from ", $thisGroupingDir, "\n";
		opendir( GROUPINGDIR, $thisGroupingDir ) or die $!;
		@imageFiles = readdir(GROUPINGDIR);
		closedir(GROUPINGDIR);

		# read all the PNG images in the grouping directory
		@images = ();
		foreach $imageFile (@imageFiles) {

			# is a file and extension is png?
			if (   length($imageFile) >= 3
				&& substr( $imageFile, -4 ) eq ".png"
				&& -f $thisGroupingDir . '/' . $imageFile )
			{
				push( @images, $imageFile );
			}
		}

		# shuffle the images
		@images = shuffle @images;

		# get the first $numImagesFromEachGroup images and read them in
		for (
			$i = 0 ;
			$i < $numImagesFromEachGroup && $i < scalar(@images) ;
			$i++
		  )
		{
			$imageLabel = $images[$i];
			print "\t\t$imageLabel\n";
			$imageLabel =~ s/^[A-Za-z]*_([0-9]*)_.*/\1/;

			# absolute path for this image
			$thisImage = $thisGroupingDir . "/" . $images[$i];

			# read the image into the image magick object
			$sketchImage = Image::Magick->new;
			$sketchImage->Read($thisImage);
			$sketchImage->Set( label => $imageLabel );

			push( @$groupingImage, $sketchImage );

			undef $sketchImage;
		}

		# montage all the images together into one image.
		# tile == sets the layout of the images in the montage. '5x' would mean that we have 5 images per row.
		# geometry == sets resolution/size of images. We force things (!) to be 200x200.
		$groupingImage = $groupingImage->Montage(
			tile     => $numImageCols . "x$numImageRows",
			geometry => $imageSize . "x$imageSize!"
		);
		$groupingImage->Set( label => $groupingDir );

		$groupingMontageFile = $thisConstraintDir . "/" . $groupingDir . ".png";

	   # write the image, which is now a montage, to the montage image file path
		$groupingImage->Write( filename => $groupingMontageFile );

		# push this grouping image onto the constraint image's image stack, so we can make one column of all the groups
		# for the constraint
		push( @$constraintImage, $groupingImage );

		# undef the grouping image to free up its memory now that we have written it to disk
		undef $groupingImage;
	}

	print
	  "Stitching all groupings for $constraintDir together into one image\n";

	# montage all the grouping images together
	$groupingImageWidth  = $numImageCols * $imageSize;
	$groupingImageHeight = $numImageRows * $imageSize;
	$constraintImageFile = "$imageDirectory/$constraintDir.png";
	$constraintImage     = $constraintImage->Montage(
		tile     => $constraintImageTile,
		frame    => '5x5',
		geometry => $groupingImageWidth . "x$groupingImageHeight+0+10"
	);
	$constraintImage->Set( label => $constraintDir );
	$constraintImage->Write($constraintImageFile);

	# add this constraint image to the set of report images
	#push(@$reportImage, $constraintImage);

	undef $constraintImage;
}

#$reportImage = $reportImage->Montage(tile=>'x100', frame=>'5x5', geometry=>'+10+0');
#$reportImage->Write($reportDestinationFile);
undef $reportImage;

print "\n\nScript completed...";

#################### END OF MAIN, BEGIN SUBROUTINES

# Read only valid directories
sub ReadValidDirectories {
	my ($dir) = @_;

	opendir( DIR, $dir ) or die $!;
	my @temp_dirs = readdir(DIR);
	closedir(DIR);

	my @validDirs = ();
	foreach my $dirListing (@temp_dirs) {
		if (   !( $dirListing eq "." )
			&& !( $dirListing eq ".." )
			&& ( -d $dir . "/$dirListing" ) )
		{
			push( @validDirs, $dirListing );
		}
	}
	undef @temp_dirs;

	return @validDirs;
}
