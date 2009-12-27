package com.sample.snippet

import scala.xml.{NodeSeq, Text}
import net.liftweb.common.{Box, Full}
import net.liftweb.http.{RequestVar, S, SHtml, TemplateFinder}
import net.liftweb.util.{Helpers, Log}
import Helpers._

import com.sample.model.Item


class AdminItems {
    private val ITEM_TEMPLATE = "templates-hidden/item_form"

    private object ShowAddItem  extends RequestVar[Int](0)
    private object SelectedItem extends RequestVar[Long](0)

    def clear = {
        ShowAddItem(0)
        SelectedItem(0)
    }

    def showAddItem(node: NodeSeq): NodeSeq = {
        ShowAddItem.get match {
            case 1 => {
                var name = ""
                val template: NodeSeq = TemplateFinder.findAnyTemplate(ITEM_TEMPLATE :: Nil).openOr(<p></p>)
                val content = bind("itemForm", template, "title"  -> Text("New item"),
                                                          "name"   -> SHtml.text(name, name = _),
                                                          "id"     -> Text(""),
                                                          "submit" -> SHtml.submit("save", () => addItem(name)),
                                                          "close"  -> SHtml.link("index", () => clear, Text("close")))

                <div>{content}</div>
            }
            case _ => SHtml.link("index", () => ShowAddItem(1), <p>Add</p>)
        }
    }

    def showEditItem(node: NodeSeq): NodeSeq = {
        if (SelectedItem.get > 0) {
            var id = SelectedItem.get
            val item = Item.find(id).open_!

            var name = item.name.is

            val template = TemplateFinder.findAnyTemplate(ITEM_TEMPLATE :: Nil).openOr(<p></p>)
            val content  = bind("itemForm", template, "title"  -> Text("Edit item"),
                                                      "name"   -> SHtml.text(name, name = _),
                                                      "id"     -> SHtml.hidden(() => {id = id}),
                                                      "submit" -> SHtml.submit("save", () => saveItem(id, name)),
                                                      "close"  -> SHtml.link("index", () => clear, Text("close")))

            <div>{content}</div>
        }
        else {
            <div></div>
        }
    }

    def list(node: NodeSeq): NodeSeq = {
        Item.findAll match {
            case Nil   => Text("There is no items in database")
            case items => items.flatMap(i => bind("item", node, "name"   -> {i.name},
                                                                "edit"   -> getEditLink(i.id),
                                                                "remove" -> getRemoveLink(i.id)))
        }
    }


    private def addItem(name: String): Any = {
        val item = new Item
        item.name(name)

        saveItem(item)
    }

    private def saveItem(id: Long, name: String): Any = {
        val item = Item.find(id).open_!
        item.name(name)

        saveItem(item)
    }

    private def saveItem(item: Item): Any = {
        try {
            item.validate match {
                case Nil => {
                    item.save
                    S.notice("Item saved")
                }
                case errors => {
                    errors.foreach(e => S.error(e.toString))
                }
            }
        }
        catch {
            case e: Exception => {
                S.error(e.getMessage)
                S.redirectTo("/items/index")
            }
        }
    }

    private def removeItem(id: Long): () => Any = {
        () => {
            try {
                val item = Item.find(id).open_!
                item.delete_!

                S.notice("Item deleted")
            }
            catch {
                case e: Exception => {
                    S.error(e.getMessage)
                }
            }
        }
    }

    private def getEditLink(id: Long): NodeSeq = {
        SHtml.link("index", () => {SelectedItem(id)}, Text("edit"))
    }

    private def getRemoveLink(id: Long): NodeSeq = {
        SHtml.link("index", removeItem(id), Text("delete"))
    }
}

