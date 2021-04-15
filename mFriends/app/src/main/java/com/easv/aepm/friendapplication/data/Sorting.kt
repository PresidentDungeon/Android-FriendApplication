package com.easv.aepm.friendapplication.data

enum class Sorting(val query: String) {

    //Sorting by...
    SORTING_NAME(" order by name ASC"),
    SORTING_AGE(" order by birthdate ASC")
}