package com.ralphdugue.phitoarch.clean

interface Mapper<P, T> {

    fun map(param: P): T
}