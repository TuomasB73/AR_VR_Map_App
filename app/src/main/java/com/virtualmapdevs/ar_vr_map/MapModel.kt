package com.virtualmapdevs.ar_vr_map

class MapModel(mapId: Int?, mapName: String?) {
    private var mapId: Int
    private var mapName: String

    init {
        this.mapId = mapId!!
        this.mapName = mapName!!

    }
    fun getMapId(): Int {
        return mapId
    }
    fun setMapId(mapId: Int?) {
        this.mapId = mapId!!
    }
    fun getMapName(): String {
        return mapName
    }
    fun setMapName(mapName: String?) {
        this.mapName = mapName!!
    }
}