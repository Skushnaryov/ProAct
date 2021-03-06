package com.proact.poject.serku.proact.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.proact.poject.serku.proact.*
import com.proact.poject.serku.proact.ui.adapters.requestsAdapters.ProjectRequestsAdapter
import com.proact.poject.serku.proact.viewmodels.RequestViewModel
import kotlinx.android.synthetic.main.activity_requests_project.*
import org.koin.android.viewmodel.ext.android.viewModel

class ProjectRequestsActivity : AppCompatActivity() {
    private val requestViewModel: RequestViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests_project)

        val projectId = intent.getIntExtra(POJECT_ID, -1)

        val adapter = ProjectRequestsAdapter(
            accepListener = { requestViewModel.updateStatus(it, 1) },
            declineListener = { requestViewModel.updateStatus(it, 2) },
            profileButtonListener = {
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra(CURRENT_USER_ID_ARG, it)
                }
                startActivity(intent)
            }
        )

        projectRequestsRv.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this)
        }

        requestViewModel.getRequestsByProject(0, projectId)


        requestViewModel.requestsByProject.observe(this, Observer {
            if (it.isEmpty()) {
                noProjectRequests.show()
            } else {
                noProjectRequests.hide()
                adapter.submitList(it)
            }
        })

        requestViewModel.isStatusUpdated.observe(this, Observer {
            requestViewModel.getRequestsByProject(0, projectId)
        })
    }
}