package com.moon.convenience_store.dto

data class ContactsDto (val NAME:String="name", val NUMBER:String="number"){
    var _ID:Long = -1
    constructor(id:Long, name:String, number:String):this(name, number){
        _ID=id;
    }
}