package com.virtualmapdevs.ar_vr_map

data class MapModel(val mapId: String?, val mapName: String?)

data class SavedMaps(var savedMaps: List<MapModel>)
