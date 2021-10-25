package com.moon.convenience_store.dto

import java.io.Serializable

class Item(var name:String, var count:Int, var regDate: Long): Serializable {
    var id=-1
    constructor(_id:Int, name:String, count:Int, regDate: Long):this(name, count, regDate){
        this.id=_id
    }

}