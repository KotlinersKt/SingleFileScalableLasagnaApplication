package com.kotliners.appkt

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kotliners.appkt.databinding.ActivityEmokisBinding
import com.kotliners.appkt.databinding.ActivityMainBinding
import com.kotliners.appkt.databinding.ItemEmokiBinding
import kotlin.properties.Delegates


///////////////////////// ACTIVITY START

@Suppress("UNCHECKED_CAST")
abstract class BaseActivity<in BINDING : ViewBinding> : AppCompatActivity() {
    abstract val binding: ViewBinding

    abstract fun onBinding(): BINDING.() -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(binding) {
            setContentView(root)
            val b = onBinding()
            b.invoke(binding as BINDING)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.kill()
    }

}

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onStart() {
        super.onStart()
        EmokisSingleton.owner = this@MainActivity
    }

    override fun onBinding(): ActivityMainBinding.() -> Unit = {
        toolbarApp.title = getString(R.string.emoki)
        setSupportActionBar(toolbarApp)
        btnSaveEmoki.setOnClickListener {
            EmokisSingleton.viewModel?.saveEmoki(edtxtEmoki.text.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_emokis -> {
                launchActivity<EmokisActivity>()
            }
        }
        return super.onOptionsItemSelected(item)
    }


}

class EmokisActivity : BaseActivity<ActivityEmokisBinding>() {

    override val binding by lazy {
        ActivityEmokisBinding.inflate(layoutInflater)
    }

    private val adapter by lazy {
        EmokiAdapter()
    }

    override fun onStart() {
        super.onStart()
        EmokisSingleton.viewModel?.emokis?.let {
            adapter.data = it
        }
    }

    override fun onBinding(): ActivityEmokisBinding.() -> Unit = {

        rcEmokis.layoutManager = LinearLayoutManager(this@EmokisActivity)
        rcEmokis.adapter = adapter
    }

}

///////////////////////// ACTIVITY END

///////////////////////// SINGLETON START

object EmokisSingleton {

    var owner: AppCompatActivity? = null

    val viewModel by lazy {
        owner?.let {
            ViewModelProvider(
                it,
                DataViewModel.DataViewModelFactory()
            ).get(DataViewModel::class.java)
        }
    }
}

///////////////////////// SINGLETON END

///////////////////////// VIEWMODEL START

class DataViewModel : ViewModel() {

    private val _emokis = mutableListOf<Emoki>()
    val emokis: List<Emoki> = _emokis

    fun saveEmoki(emoki: String) {
        _emokis.add(Emoki(emoki))
    }

    class DataViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DataViewModel() as T
        }
    }

}

///////////////////////// VIEWMODEL END


///////////////////////// ADAPTER START

class EmokiAdapter : RecyclerView.Adapter<EmokiAdapter.ViewHolder>() {

    var data by Delegates.observable(listOf<Emoki>()) { _, old, newList ->
        old + newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflated = parent.inflateFrom()
        return ViewHolder(inflated)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    class ViewHolder(private val binding: ItemEmokiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(emoki: Emoki) = with(binding) {
            txtItemEmoki.text = emoki.emoki
        }
    }
}

///////////////////////// ADAPTER END

///////////////////////// DATA CLASS START

data class Emoki(val emoki: String)

///////////////////////// DATA CLASS END

///////////////////////// EXTENSION FUNCTIONS START

inline fun <reified T> FragmentActivity.launchActivity(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.block()
    startActivity(intent)
}

fun ViewGroup.inflateFrom() =
    ItemEmokiBinding.inflate(LayoutInflater.from(this.context))

private fun ViewBinding.kill() {

}

///////////////////////// EXTENSION FUNCTIONS END
