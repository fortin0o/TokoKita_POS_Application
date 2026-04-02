package model

import android.os.Parcel
import android.os.Parcelable
import model.modelKategori

data class modelProduk (
    var idProduk: String? = null,
    var namaProduk: String? = null,
    var deskripsiProduk: String? = null,
    var hargaProduk: Int? = 0,
    var idKategori: String? = null,
    var statusProduk: String? = null,
    var stokProduk: Int? = 0,
    var fotoProduk: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idProduk)
        parcel.writeString(namaProduk)
        parcel.writeString(deskripsiProduk)
        parcel.writeValue(hargaProduk)
        parcel.writeString(idKategori)
        parcel.writeString(statusProduk)
        parcel.writeValue(stokProduk)
        parcel.writeString(fotoProduk)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<modelProduk> {
        override fun createFromParcel(parcel: Parcel): modelProduk {
            return modelProduk(parcel)
        }

        override fun newArray(size: Int): Array<modelProduk?> {
            return arrayOfNulls(size)
        }
    }
}

