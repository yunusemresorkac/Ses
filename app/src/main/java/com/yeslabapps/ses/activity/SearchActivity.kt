package com.yeslabapps.ses.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.yeslabapps.ses.R
import com.yeslabapps.ses.databinding.ActivitySearchBinding
import com.yeslabapps.ses.fragment.SearchUserFragment
import com.yeslabapps.ses.fragment.SearchVoiceFragmentByTag
import com.yeslabapps.ses.fragment.SearchVoiceFragmentByTitle
import kotlinx.android.synthetic.main.activity_search.*
import java.util.*


class SearchActivity : AppCompatActivity(){

    private lateinit var binding: ActivitySearchBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }


        val fragments = listOf(SearchUserFragment(),
            SearchVoiceFragmentByTitle())

        val adapter = ViewPagerAdapter(this, fragments)
        viewPager.adapter = adapter

        TabLayoutMediator(binding.tabsSearch, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Users"
                1 -> "Voices"
                else -> ""
            }
        }.attach()







    }


    class ViewPagerAdapter(
        fragmentActivity: FragmentActivity,
        private val fragments: List<Fragment>
    ) : FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }


}