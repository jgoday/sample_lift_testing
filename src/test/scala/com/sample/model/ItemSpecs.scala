package com.sample.model

import net.liftweb.mapper.{Schemifier}
import net.liftweb.util.{Log}

import com.sample.utils.DBUtil

import org.specs.Specification
import org.specs.runner.JUnit4

class ItemSpecsAsTest extends JUnit4(ItemSpecs)

object ItemSpecs extends Specification {
    "Item" should {
        doFirst {
            DBUtil.initialize
            Schemifier.schemify(true, Log.infoF _ , Item)
            DBUtil.setupDB("dbunit/item_test.xml")
        }

        "save without problem" in {
            val item = new Item
            item.name("item name")

            item.save must beTrue
            (Item.findAll.length == 3) must beTrue
        }

        "find by name" in {
            val items = Item.findByName("Item 1")
            items.length must_== 1
        }

        "delete without problem" in {
            val items = Item.findByName("item name")

            items.length must_== 1
            items(0).delete_! must beTrue
            Item.findAll.length must_== 2
        }

        doLast {
            DBUtil.shutdownDB
            Schemifier.destroyTables_!!(Log.infoF _, Item)
        }
    }
}
