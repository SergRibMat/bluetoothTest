package com.example.android.bluetoothtest.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

fun askForPermissionWithDexter(context: Context, permission: String, permissionTitle: String ){
    Dexter.withContext(context)
            .withPermission(
                    permission
            )
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    showMessage(context, "Permission granted")
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    showMessage(context,"Permission denied")
                }

                override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, token: PermissionToken?) {
                    showMessage(context,"onPermissionRationaleShouldBeShown")

                    AlertDialog.Builder(context)
                            .setTitle("Give Permission $permissionTitle")
                            .setMessage("You need to give permission $permissionTitle for this app")
                            .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener {
                                dialogInterface, i ->
                                dialogInterface.dismiss()
                                token?.cancelPermissionRequest()
                            })
                            .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener {
                                dialogInterface, i ->
                                dialogInterface.dismiss()
                                token?.continuePermissionRequest()
                            })
                            .show()
                }

            })
            .check()
}