package com.example.a52swoahelper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.snapshots.Snapshot
import com.example.a52swoahelper.Commands.Companion.adjustIndexList
import com.example.a52swoahelper.Commands.Companion.cleanIndexList
import com.example.a52swoahelper.Commands.Companion.context
import com.example.a52swoahelper.Commands.Companion.executeCommand
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    lateinit var bootIntoWindowsButton: LinearLayout
    lateinit var mountWindowsButton: LinearLayout
    lateinit var helpButton: Button
    lateinit var backupBootButton: LinearLayout
    lateinit var installWindowsButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        Commands.context = this

        Files.createFolderIfFolderDontExists("/sdcard/WindowsInstall", this)
        Files.createFolderIfFolderDontExists("/sdcard/UEFI", this)

        bootIntoWindowsButton = findViewById(R.id.BootIntoWindowsButton)
        bootIntoWindowsButton.setOnClickListener{
            val standartDialog = StandartDialog(this)
            standartDialog.showDialog(
                imageResId = R.drawable.win11logo,
                text = getString(R.string.boot_to_windows_dialog),
                onYesClick = {
                    Commands.bootIntoWindows(this)
                },
                onNoClick = {}
            )
        }

        mountWindowsButton = findViewById(R.id.MountWindowsButton)
        val mountWindows: TextView = findViewById(R.id.MountWindowsLabel)
        if (Commands.isWindowsMounted()) {
            mountWindows.text = getString(R.string.unmount_windows)
        }
        mountWindowsButton.setOnClickListener{
            val standartDialog = StandartDialog(this)
            standartDialog.showDialog(
                imageResId = R.drawable.folder,
                text = if (!Commands.isWindowsMounted()) getString(R.string.mount_windows_dialog) else getString(R.string.unmount_windows_dialog),
                onYesClick = {
                    if (Commands.isWindowsMounted()) {
                         Commands.executeCommand("su -mm -c umount /sdcard/Windows", true)

                        mountWindows.text = getString(R.string.mount_windows)
                        return@showDialog
                    } else {
                        Commands.mountWindows(this)


                        mountWindows.text = getString(R.string.unmount_windows)
                    }


                },
                onNoClick = {}
            )
        }

        helpButton = findViewById(R.id.HelpButton)
        helpButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/a52sxq_uefi"))
            startActivity(intent)
        }

        backupBootButton = findViewById(R.id.BackupBootButton)
        backupBootButton.setOnClickListener{
            val standartDialog = StandartDialog(this)
            standartDialog.showDialog(
                imageResId = R.drawable.cd,
                text = getString(R.string.backup_boot_image_dialog),
                onYesClick = {
                    Commands.backupBootImage()
                },
                onNoClick = {}
            )
        }

        installWindowsButton = findViewById(R.id.InstallWindowsButton)
        installWindowsButton.setOnClickListener{
            val standartDialog = StandartDialog(this)
            standartDialog.showDialog(
                imageResId = R.drawable.installer,
                text = getString(R.string.install_windows_dialog),
                onYesClick = {
                    TwoOptionDialog(this).showDialog("Select method", "Pre-made Image (fast)", "Custom Image (slower)",
                        onBtn1Click = {
                            val wimFiles = executeCommand("su -c ls /sdcard/WindowsInstall/ | grep -E \".wim|.esd\"", true)
                            var wimFilesList: List<String> = wimFiles.split("\n")
                            wimFilesList = wimFilesList.dropLast(1)
                            wimFilesList = cleanIndexList(wimFilesList)
                            val wimFilesListRender = adjustIndexList(wimFilesList)
                            val indexDialog = IndexDialog(this)
                            indexDialog.showDialog(
                                title = "Select the wim you want to install",
                                wimFilesListRender,

                                ) {
                                val path = "/sdcard/WindowsInstall/" + wimFilesList[indexDialog.index - 1]

                                val indexes = executeCommand("su -c /data/local/tmp/wimlib-imagex info $path | grep -E \"Index|Name|Description|Display Name|Display Description\" | grep -v -E \"Boot Index|Product Name\"", true)
                                val get_index_dialog = IndexDialog(this)
                                get_index_dialog.showDialog(
                                    title = "Select index",
                                    listOf(indexes),
                                    Gravity.START
                                ) {
                                    val index = get_index_dialog.index
                                    Commands.installWindows(
                                        path,
                                        index,
                                        1
                                    )
                                }

                            }
                        },
                        onBtn2Click = {
                            val wimFiles = executeCommand("su -c ls /sdcard/WindowsInstall/ | grep -E \".wim|.esd\"", true)
                            var wimFilesList: List<String> = wimFiles.split("\n")
                            wimFilesList = wimFilesList.dropLast(1)
                            wimFilesList = cleanIndexList(wimFilesList)
                            val wimFilesListRender = adjustIndexList(wimFilesList)
                            val indexDialog = IndexDialog(this)
                            indexDialog.showDialog(
                                title = "Select the wim you want to install",
                                wimFilesListRender,

                            ) {
                                val path = "/sdcard/WindowsInstall/" + wimFilesList[indexDialog.index - 1]

                                val indexes = executeCommand("su -c /data/local/tmp/wimlib-imagex info $path | grep -E \"Index|Name|Description|Display Name|Display Description\" | grep -v -E \"Boot Index|Product Name\"", true)
                                val get_index_dialog = IndexDialog(this)
                                get_index_dialog.showDialog(
                                    title = "Select index",
                                    listOf(indexes),
                                    Gravity.START
                                ) {
                                    val index = get_index_dialog.index
                                    Commands.installWindows(
                                        path,
                                        index,
                                        2
                                    )
                                }

                            }

                        }
                    )

                },
                onNoClick = {}
            )
        }
    }

}