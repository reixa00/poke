package com.axier.poke.view.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import coil.api.load
import coil.transform.CircleCropTransformation
import com.axier.poke.R
import com.axier.poke.common.prettify
import com.axier.poke.data.entities.pokemon.PokemonEntity
import com.axier.poke.data.repository.pokemon.PokemonRepositoryImp
import com.axier.poke.view.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PokemonDetailActivity : AppCompatActivity(), BaseActivity {

    private lateinit var pokemonName: String
    private lateinit var pokemonNameTv: TextView
    private lateinit var pokemonImg: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var nextImage: ImageButton
    private lateinit var pokemonHeight: TextView
    private lateinit var pokemonWeight: TextView
    private lateinit var pokemonBaseExp: TextView
    private lateinit var pokemonTypes: TextView
    private lateinit var pokemonMoves: TextView
    private var spritesIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon_detail)

        intent.extras?.let {
            pokemonName = it.getString("pokemon_name", "")
        }

        findUIElements()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = pokemonName.prettify()

        doGetPokemonRequest(pokemonName)

    }

    private fun findUIElements() {
        toolbar = findViewById(R.id.toolbar)
        pokemonNameTv = findViewById(R.id.pokemon_name)
        pokemonImg = findViewById(R.id.pokemon_image)
        nextImage = findViewById(R.id.pokemon_next_image)
        pokemonHeight = findViewById(R.id.pokemon_height)
        pokemonWeight = findViewById(R.id.pokemon_weight)
        pokemonBaseExp = findViewById(R.id.pokemon_base_exp)
        pokemonTypes = findViewById(R.id.pokemon_types)
        pokemonMoves = findViewById(R.id.pokemon_moves)
    }

    private fun doGetPokemonRequest(pokemonName: String?) {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            val pokemon = PokemonRepositoryImp(applicationContext).getPokemon(pokemonName)
            launch(Dispatchers.Main){
                when (pokemon) {
                    null -> showToast(R.string.http_generic_error)
                    else -> {
                        loadPokemonUIData(pokemon)
                    }
                }
            }

        }
    }

    private fun loadPokemonUIData(pokemon: PokemonEntity.Pokemon) {
        pokemonNameTv.text = pokemon.name.prettify()
        pokemonHeight.text = getString(R.string.pokemon_height, pokemon.height)
        pokemonWeight.text = getString(R.string.pokemon_weight, pokemon.weight)
        pokemonBaseExp.text = getString(R.string.pokemon_base_ext, pokemon.baseExperience)
        pokemonTypes.text = getString(R.string.pokemon_types, pokemon.types?.map { it.type?.name }?.joinToString())

        val moves = pokemon.moves?.map { it.move.name.replace("-", " ") }
        moves?.forEach {
            pokemonMoves.append(it)
            pokemonMoves.append(System.lineSeparator())
        }

        var sprites = loadSpritesToList(pokemon.sprites)
        sprites = sprites.filterNot { it == null }
        if (sprites.isNotEmpty()) {
            loadPokemonImage(sprites[0])
        }
        nextImage.setOnClickListener {
            if (spritesIndex < sprites.size - 1) {
                spritesIndex++
            } else {
                spritesIndex = 0
            }
            loadPokemonImage(sprites[spritesIndex])
        }
    }

    private fun loadSpritesToList(sprites: PokemonEntity.PokemonSprite?): List<String?> {
        return listOf(
            sprites?.backDefault,
            sprites?.backFemale,
            sprites?.backShiny,
            sprites?.backShinyFemale,
            sprites?.frontDefault,
            sprites?.frontFemale,
            sprites?.frontShiny,
            sprites?.frontShinyFemale
        )
    }

    private fun loadPokemonImage(url: String?) {
        pokemonImg.load(url) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
    }

    private fun showToast(stringRes: Int) {
        Toast.makeText(
            this@PokemonDetailActivity,
            stringRes,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
