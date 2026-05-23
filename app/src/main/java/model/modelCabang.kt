package model

import android.os.Parcel
import android.os.Parcelable

data class modelCabang(
    var idCabang: String? = null,
    var namaCabang: String? = null,
    var alamatCabang: String? = null,
    var statusCabang: String? = "Pusat"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idCabang)
        parcel.writeString(namaCabang)
        parcel.writeString(alamatCabang)
        parcel.writeString(statusCabang)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<modelCabang> {
        override fun createFromParcel(parcel: Parcel): modelCabang {
            return modelCabang(parcel)
        }

        override fun newArray(size: Int): Array<modelCabang?> {
            return arrayOfNulls(size)
        }
    }
}
