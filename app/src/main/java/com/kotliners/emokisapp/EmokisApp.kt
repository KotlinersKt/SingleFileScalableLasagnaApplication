package com.kotliners.emokisapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kotliners.emokisapp.databinding.ActivityEmokisBinding
import com.kotliners.emokisapp.databinding.ActivityMainBinding
import com.kotliners.emokisapp.databinding.ItemEmokiBinding
import kotlin.properties.Delegates


///////////////////////// ACTIVITY START


internal class GorroActivity : BaseActivity<ActivityMainBinding>() {
    override val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override val onBinding: ActivityMainBinding.() -> Unit = {}
}

/**
 * Descripción corta
 *
 * Esta es una descripción mas completa que ya no es el título
 *
 * **Esta clase es la base de nuestras actividades...en letras negritas**
 *
 * @author KotlinersKT
 * @since 1.0.0
 * @param BINDING es un super genérico subclase de [androidx.viewbinding.ViewBinding] Vinculación de Vista
 *
 * @see [androidx.viewbinding.ViewBinding]
 *
 * @sample [com.kotliners.emokisapp.GorroActivity]
 * @sample [com.kotliners.emokisapp.EmokisActivity]
 */
@Suppress("UNCHECKED_CAST")
abstract class BaseActivity<BINDING : ViewBinding> : AppCompatActivity() {

    /**
     * Es la referencia del objeto generado por la libreria de [Vinculación de vista](https://developer.android.com/topic/libraries/view-binding?hl=es-419)
     */
    abstract val binding: BINDING

    /**
     * Es el metodo donde puedes acceder a las vistas del objecto ViewBinding y
     * configurarlos
     */
    abstract val onBinding: BINDING.() -> Unit

    protected val emokisSingleton: EmokisSingleton by injektor.injekt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(binding) {
            setContentView(root)
            onBinding()
        }
    }

    fun showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, length).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.kill()
    }

}

class MainActivity : BaseActivity<ActivityMainBinding>() {

    /**
     *
     * @see [com.kotliners.emokisapp.databinding.ActivityMainBinding]
     */
    override
    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    val emoki: Emoki by injektor.injekt()

    override fun onStart() {
        super.onStart()
        emokisSingleton.owner = this@MainActivity
    }

    override val onBinding: ActivityMainBinding.() -> Unit = {
        toolbarApp.title = getString(R.string.emoki)
        setSupportActionBar(toolbarApp)
        edtxtEmoki.setText(emoki.emoki)
        btnSaveEmoki.setOnClickListener {
            emokisSingleton.viewModel?.saveEmoki(edtxtEmoki.text.toString())
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
        EmokiAdapter(this::onClicktem)
    }

    override fun onStart() {
        super.onStart()
        emokisSingleton.viewModel?.emokis?.let {
            adapter.data = it
        }
    }

    override val onBinding: ActivityEmokisBinding.() -> Unit = {

        rcEmokis.layoutManager = LinearLayoutManager(this@EmokisActivity)
        rcEmokis.adapter = adapter
    }

    fun onClicktem(emoki: Emoki) {
        val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("EmokiApp", emoki.emoki)
        clipBoard.setPrimaryClip(clip)
        showToast("Emoki copeaaaaado a klipboard :D")
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

    @Suppress("UNCHECKED_CAST")
    class DataViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DataViewModel() as T
        }
    }

}

///////////////////////// VIEWMODEL END


///////////////////////// ADAPTER START
typealias EmokiClickListener = (Emoki) -> Unit

class EmokiAdapter(private val onClickItem: EmokiClickListener) :
    RecyclerView.Adapter<EmokiAdapter.ViewHolder>() {

    var data by Delegates.observable(listOf<Emoki>()) { _, old, newList ->
        old + newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflated = parent.inflateFrom()
        return ViewHolder(inflated, onClickItem)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    class ViewHolder(
        private val binding: ItemEmokiBinding,
        private val emokiClickListener: EmokiClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(emoki: Emoki) = with(binding) {
            itemView.setOnClickListener { emokiClickListener(emoki) }
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

/**
 *
 * @return [ItemEmokiBinding]
 */
fun ViewGroup.inflateFrom(): ItemEmokiBinding =
    ItemEmokiBinding.inflate(LayoutInflater.from(this.context))

private fun ViewBinding.kill() {

}

///////////////////////// EXTENSION FUNCTIONS END

///////////////////////// DEPENDENCY INYECTION START


class ElContenedor {

    val dependenkies = mutableMapOf<String, Any>()

}

class Inllector private constructor() {

    val elContenedor = ElContenedor()

    inline fun <reified T> injekt(): Lazy<T> {
        return lazyOf(elContenedor.dependenkies[T::class.java.simpleName] as T)
    }

    companion object {
        val inllektor = Inllector()
        inline fun <reified E : Any> inllektorOf(vararg deks: E): Inllector {
            for (dek in deks) {
                inllektor.elContenedor.dependenkies[dek::class.java.simpleName] = dek
            }
            return inllektor
        }

        inline fun <reified R : Any> registerDeks(dek: R) {
            inllektor.elContenedor.dependenkies[dek::class.java.simpleName] = dek
        }
    }

}

private val inllektor = Inllector.inllektorOf(EmokisSingleton, Emoki("🙄"), "Chuby")
val FragmentActivity.injektor: Inllector
    get() = inllektor

///////////////////////// DEPENDENCY INYECTION END

