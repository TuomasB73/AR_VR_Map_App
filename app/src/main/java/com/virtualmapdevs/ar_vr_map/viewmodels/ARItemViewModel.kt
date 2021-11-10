package com.virtualmapdevs.ar_vr_map.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.virtualmapdevs.ar_vr_map.model.ARItemModel

class ARItemViewModel : ViewModel() {
    val arItem = MutableLiveData<ARItemModel>()

    fun getARItem(itemId: Int) {
        arItem.value = ARItemModel(1234, "Placeholder title", "Placeholder description",
            "https://users.metropolia.fi/~tuomasbb/mobile_project/test_3d_model/terrain_example.gltf")
    }
}