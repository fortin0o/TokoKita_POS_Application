package model

import android.os.Parcel
import android.os.Parcelable

data class modelCartItem(
    var produk: modelProduk? = null,
    var jumlah: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(modelProduk::class.java.classLoader),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(produk, flags)
        parcel.writeInt(jumlah)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<modelCartItem> {
        override fun createFromParcel(parcel: Parcel): modelCartItem {
            return modelCartItem(parcel)
        }

        override fun newArray(size: Int): Array<modelCartItem?> {
            return arrayOfNulls(size)
        }
    }
}
