package model

import android.os.Parcel
import android.os.Parcelable

data class modelPegawai(
    var idPegawai: String? = null,
    var namaPegawai: String? = null,
    var phonePegawai: String? = null,
    var rolePegawai: String? = null,
    var statusPegawai: String? = "Aktif"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idPegawai)
        parcel.writeString(namaPegawai)
        parcel.writeString(phonePegawai)
        parcel.writeString(rolePegawai)
        parcel.writeString(statusPegawai)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<modelPegawai> {
        override fun createFromParcel(parcel: Parcel): modelPegawai {
            return modelPegawai(parcel)
        }

        override fun newArray(size: Int): Array<modelPegawai?> {
            return arrayOfNulls(size)
        }
    }
}
