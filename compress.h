//
// Created by Slang on 9/6/2018.
//

#ifndef WILF6_COMPRESS_H
#define WILF6_COMPRESS_H

#include <gmp.h>

//an array that stores the difference between numbers in the appropriate size
typedef union pointer{
    signed char* schar;
    short* sshort;
    int* sint;
    long* slongp;
    unsigned long* ulongp;
} pointer;

typedef struct cmp {
    unsigned int size; //size in bytes
    pointer beginning;
} cmp;

#define mpz_add_si(x,y,z) ((z) < 0 ? (mpz_sub_ui(x,y,(unsigned long) -(z))) : (mpz_add_ui(x,y,(unsigned long)(z))))
#define fmpz_add_si(x,y,z) ((z) < 0 ? (fmpz_sub_ui(x,y,(unsigned long) -(z))) : (fmpz_add_ui(x,y,(unsigned long)(z))))
#define mpz_fits_schar_p(x) (mpz_fits_sshort_p(x) && (mpz_get_si(x) <= SCHAR_MAX) && (mpz_get_si(x) > SCHAR_MIN))
#define fmpz_fits_schar_p(x) (fmpz_fits_si(x) && (fmpz_get_si(x) <= SCHAR_MAX) && (fmpz_get_si(x) > SCHAR_MIN))
#endif //WILF6_COMPRESS_H
