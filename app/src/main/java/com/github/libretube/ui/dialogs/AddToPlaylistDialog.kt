package com.github.libretube.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.libretube.R
import com.github.libretube.api.RetrofitInstance
import com.github.libretube.api.obj.PlaylistId
import com.github.libretube.constants.IntentData
import com.github.libretube.databinding.DialogAddtoplaylistBinding
import com.github.libretube.extensions.TAG
import com.github.libretube.extensions.toastFromMainThread
import com.github.libretube.ui.models.PlaylistViewModel
import com.github.libretube.util.PreferenceHelper
import com.github.libretube.util.ThemeHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AddToPlaylistDialog : DialogFragment() {
    private lateinit var binding: DialogAddtoplaylistBinding
    private val viewModel: PlaylistViewModel by activityViewModels()

    private lateinit var videoId: String
    private lateinit var token: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        videoId = arguments?.getString(IntentData.videoId)!!
        binding = DialogAddtoplaylistBinding.inflate(layoutInflater)
        binding.title.text = ThemeHelper.getStyledAppName(requireContext())

        token = PreferenceHelper.getToken()

        if (token != "") fetchPlaylists()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .show()
    }

    private fun fetchPlaylists() {
        lifecycleScope.launchWhenCreated {
            val response = try {
                RetrofitInstance.authApi.playlists(token)
            } catch (e: IOException) {
                println(e)
                Log.e(TAG(), "IOException, you might not have internet connection")
                Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show()
                return@launchWhenCreated
            } catch (e: HttpException) {
                Log.e(TAG(), "HttpException, unexpected response")
                Toast.makeText(context, R.string.server_error, Toast.LENGTH_SHORT).show()
                return@launchWhenCreated
            }
            if (response.isNotEmpty()) {
                val names = response.map { it.name }
                val arrayAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                arrayAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )
                binding.playlistsSpinner.adapter = arrayAdapter
                if (viewModel.lastSelectedPlaylistId != null) {
                    var selectionIndex = 0
                    response.forEachIndexed { index, playlist ->
                        if (playlist.id == viewModel.lastSelectedPlaylistId) {
                            selectionIndex =
                                index
                        }
                    }
                    binding.playlistsSpinner.setSelection(selectionIndex)
                }
                runOnUiThread {
                    binding.addToPlaylist.setOnClickListener {
                        val index = binding.playlistsSpinner.selectedItemPosition
                        viewModel.lastSelectedPlaylistId = response[index].id!!
                        addToPlaylist(response[index].id!!)
                        dialog?.dismiss()
                    }
                }
            }
        }
    }

    private fun addToPlaylist(playlistId: String) {
        val appContext = context?.applicationContext ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                RetrofitInstance.authApi.addToPlaylist(
                    token,
                    PlaylistId(playlistId, videoId)
                )
            } catch (e: IOException) {
                println(e)
                Log.e(TAG(), "IOException, you might not have internet connection")
                appContext.toastFromMainThread(R.string.unknown_error)
                return@launch
            } catch (e: HttpException) {
                Log.e(TAG(), "HttpException, unexpected response")
                appContext.toastFromMainThread(R.string.server_error)
                return@launch
            }
            appContext.toastFromMainThread(
                if (response.message == "ok") R.string.added_to_playlist else R.string.fail
            )
        }
    }

    private fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }
}
