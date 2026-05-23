package model

import android.os.Parcel
import android.os.Parcelable

data class modelProduk (
    var idProduk: String? = null,
    var namaProduk: String? = null,
    var deskripsiProduk: String? = null,

    var hargaBeli: Int? = 0,
    var tipeKeuntungan: String? = null,
    var nilaiProfit: Double? = 0.0,
    var hargaJual: Int? = 0,

    var idKategori: String? = null,
    var idCabang: String? = null,
    var statusProduk: String? = "Aktif",

    var stokProduk: Int? = 0,
    var tanpaBatas: Boolean? = false,

    var barcode: String? = null,
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
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,

        parcel.readString(),
        parcel.readString(),
        parcel.readString(),

        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,

        parcel.readString(),
        parcel.readString(),

        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idProduk)
        parcel.writeString(namaProduk)
        parcel.writeString(deskripsiProduk)

        parcel.writeValue(hargaBeli)
        parcel.writeString(tipeKeuntungan)
        parcel.writeValue(nilaiProfit)
        parcel.writeValue(hargaJual)

        parcel.writeString(idKategori)
        parcel.writeString(idCabang)
        parcel.writeString(statusProduk)

        parcel.writeValue(stokProduk)
        parcel.writeValue(tanpaBatas)

        parcel.writeString(barcode)
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
