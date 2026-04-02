package model

import android.os.Parcel
import android.os.Parcelable

data class modelKategori(
    var idKategori: String? = null,
    var namaKategori: String? = null,
    var statusKategori: String? = null
) : Parcelable {
    // Constructor dari Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idKategori)
        parcel.writeString(namaKategori)
        parcel.writeString(statusKategori)
    }

    companion object CREATOR : Parcelable.Creator<modelKategori> {
        override fun createFromParcel(parcel: Parcel): modelKategori {
            return modelKategori(parcel)
        }

        override fun newArray(size: Int): Array<modelKategori?> {
            return arrayOfNulls(size)
        }
    }
}