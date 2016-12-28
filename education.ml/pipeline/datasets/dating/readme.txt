SUMMARY
================================================================================

These files contain 17,359,346 anonymous ratings of 168,791 profiles
made by 135,359 LibimSeTi users as dumped on April 4, 2006.

The data is available from

	http://www.ksi.ms.mff.cuni.cz/~petricek/data/

USAGE LICENSE
================================================================================

Neither the Charles University nor any of the researchers
involved can guarantee the correctness of the data, its suitability
for any particular purpose, or the validity of results based on the
use of the data set.  The data set may be used for any research
purposes under the following conditions:

     * The user may not state or imply any endorsement from the
       Charles University or LibimSeTi.cz.

     * The user must acknowledge the use of the data set in
       publications resulting from the use of the data set, and must
       send us an electronic or paper copy of those publications.

     * The user may not redistribute the data without separate
       permission.

     * The user may not use this information for any commercial or
       revenue-bearing purposes without first obtaining permission
       from LibimSeTi.cz.

If you have any further questions or comments, please contact Vaclav Petricek
<petricek@acm.org>. 

ACKNOWLEDGEMENTS
================================================================================

Thanks to Oldrich Neuberger for providing the data and
Lukas Brozovsky for cleaning up and generating the data set.

LibimSeTi.cz currently operates a dating website:

        http://www.libimseti.cz/

RATINGS FILE DESCRIPTION
================================================================================

All ratings are contained in the file "ratings.dat" and are in the
following format:

UserID,ProfileID,Rating

- UserID is user who provided rating
- ProfileID is user who has been rated
- UserIDs range between 1 and 135,359
- ProfileIDs range between 1 and 220,970 (not every profile has been rated)
- Ratings are on a 1-10 scale where 10 is best (integer ratings only)
- Only users who provided at least 20 ratings were included
- Users who provided constant ratings were excluded

USERS FILE DESCRIPTION
================================================================================

User gender information is in the file "gender.dat" and is in the following
format:

UserID,Gender

- Gender is denoted by a "M" for male and "F" for female and "U" for unknown

CITATION
================================================================================

@inproceedings{brozovsky07recommender,
    author = {Lukas Brozovsky and Vaclav Petricek},
    title = {Recommender System for Online Dating Service},
    booktitle = {Proceedings of Znalosti 2007 Conference},
    year = {2007},
    isbn = {},
    pages = {},
    location = {Ostrava, Czech Republic},
    publisher = {VSB},
    address = {Ostrava},
}
