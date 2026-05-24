package model

import android.os.Parcel
import android.os.Parcelable

data class modelPelanggan(
    var idPelanggan: String? = null,
    var namaPelanggan: String? = null,
    var phonePelanggan: String? = null,
    var alamatPelanggan: String? = null,
    var statusPelanggan: String? = "Aktif"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idPelanggan)
        parcel.writeString(namaPelanggan)
        parcel.writeString(phonePelanggan)
        parcel.writeString(alamatPelanggan)
        parcel.writeString(statusPelanggan)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<modelPelanggan> {
        override fun createFromParcel(parcel: Parcel): modelPelanggan {
            return modelPelanggan(parcel)
        }

        override fun newArray(size: Int): Array<modelPelanggan?> {
            return arrayOfNulls(size)
        }
    }
}
