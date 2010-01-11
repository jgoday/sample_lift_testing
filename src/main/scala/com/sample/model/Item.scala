package com.sample.model

import net.liftweb.mapper.{By, IdPK,
                           LongKeyedMapper, LongKeyedMetaMapper,
                           MappedString}

class Item extends LongKeyedMapper[Item] with IdPK
                                         with ChangeLogger[Item] {
    def getSingleton = Item

    object name extends MappedString(this, 100) {
        override def validations = valMinLen(1, "Must be not empty") _ ::
                                   valUnique("Name must be unique") _ ::
                                   super.validations
    }
}

object Item extends Item with ChangeLoggerMetaMapper[Item] {
    def findByName(name: String): List[Item] = {
        Item.findAll(By(Item.name, name))
    }
}
