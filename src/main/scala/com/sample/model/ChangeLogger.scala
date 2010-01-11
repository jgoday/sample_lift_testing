package com.sample.model

import net.liftweb.mapper.{LongKeyedMapper, LongKeyedMetaMapper, Mapper}
import net.liftweb.util.{Log}

trait ChangeLogger[T <: Mapper[T] ] {
    def checkChanges(obj: T): Unit = {
        if (obj.dirty_?) {
            Log.info("Object " + obj.getSingleton.dbTableName + " has changed")

            obj.formFields.foreach(f => {
                f.dirty_? match {
                    case true => Log.info(" ---> Property %s was='%s' and now is = '%s'".format(f.name, f.is.toString, f.was.toString))
                }
            })
        }
    }
}

trait ChangeLoggerMetaMapper[T <: LongKeyedMapper[T]] extends LongKeyedMetaMapper[T]
                                                with ChangeLogger[T] { self : T =>
    override def afterSave: List[(T) => Unit] = checkChanges _ :: super.afterSave
}

