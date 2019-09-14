package de.westnordost.streetcomplete.data.osm.persist

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteOpenHelper

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.ktx.queryOne
import de.westnordost.streetcomplete.ktx.transaction

abstract class AOsmElementDao<T : Element>(private val dbHelper: SQLiteOpenHelper) {

	private val db get() = dbHelper.writableDatabase

	protected abstract val elementTypeName: String
    protected abstract val tableName: String
    protected abstract val idColumnName: String

    fun putAll(elements: Collection<T>) {
	    db.transaction {
		    for (element in elements) {
			    put(element)
		    }
	    }
    }

    fun put(element: T) {
	    db.insertWithOnConflict(tableName, null, createContentValuesFrom(element), CONFLICT_REPLACE)
    }

    fun delete(id: Long) {
        db.delete(tableName, "$idColumnName = $id", null)
    }

    fun get(id: Long): T? {
	    return db.queryOne(tableName, null, "$idColumnName = $id", null) { createObjectFrom(it) }
    }

    /** Cleans up element entries that are not referenced by any quest anymore.  */
    open fun deleteUnreferenced() {
        val where = """
			$idColumnName NOT IN (
			${getSelectAllElementIdsIn(OsmQuestTable.NAME)} 
			UNION
			${getSelectAllElementIdsIn(UndoOsmQuestTable.NAME)}
			)""".trimIndent()

        db.delete(tableName, where, null)
    }

    protected fun getSelectAllElementIdsIn(table: String) = """
        SELECT ${OsmQuestTable.Columns.ELEMENT_ID} AS $idColumnName
		FROM $table
        WHERE ${OsmQuestTable.Columns.ELEMENT_TYPE} = "$elementTypeName"
    """

    protected abstract fun createContentValuesFrom(element: T): ContentValues
    protected abstract fun createObjectFrom(cursor: Cursor): T
}
