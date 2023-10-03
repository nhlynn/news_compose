package com.nhlynn.news_compose.presentation.news_screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.nhlynn.news_compose.domain.model.Article
import com.nhlynn.news_compose.presentation.component.BottomSheetContent
import com.nhlynn.news_compose.presentation.component.CategoryTabRow
import com.nhlynn.news_compose.presentation.component.NewArticleCard
import com.nhlynn.news_compose.presentation.component.NewsScreenTopBar
import com.nhlynn.news_compose.presentation.component.RetryContent
import com.nhlynn.news_compose.presentation.component.SearchAppBar
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun NewsScreen(
    state: NewsScreenState,
    onEvent: (NewsScreenEvent) -> Unit,
    onReadFullScreen: (String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val categories =
        listOf("General", "Business", "Health", "Science", "Sports", "Technology", "Entertainment")
    val pagerState = rememberPagerState(pageCount = { categories.size })

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var bottomSheetShow by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    if (bottomSheetShow) {
        ModalBottomSheet(
            onDismissRequest = {
                bottomSheetShow = false
            },
            sheetState = sheetState,
            content = {
                state.selectedArticle?.let { article ->
                    BottomSheetContent(
                        article = article,
                        onReadFullStoryBottomClicked = {
                            onReadFullScreen(article.url)
                            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) bottomSheetShow = false
                            }
                        }
                    )
                }
            }
        )
    }

    LaunchedEffect(key1 = pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onEvent(NewsScreenEvent.OnCategoryChanged(category = categories[page]))
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (state.searchQuery.isNotEmpty()) {
            onEvent(NewsScreenEvent.OnSearchQueryChanged(searchQuery = state.searchQuery))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = state.isSearchBarVisible) { isVisible ->
            if (isVisible) {
                Column {
                    SearchAppBar(
                        modifier = Modifier.focusRequester(focusRequester),
                        value = state.searchQuery,
                        onInputValueChange = { newValue ->
                            onEvent(NewsScreenEvent.OnSearchQueryChanged(newValue))
                        },
                        onCloseIconClicked = {
                            onEvent(NewsScreenEvent.OnCloseIconClicked)
                        },
                        onSearchIconClicked = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    )
                    NewsArticleList(
                        state = state,
                        onCardClicked = { article ->
                            bottomSheetShow = true
                            onEvent(NewsScreenEvent.OnNewsCardClicked(article = article))
                        },
                        onRetry = {
                            onEvent(NewsScreenEvent.OnCategoryChanged(state.category))
                        }
                    )
                }
            } else {
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        NewsScreenTopBar(
                            scrollBehavior = scrollBehavior,
                            onSearchIconClicked = {
                                onEvent(NewsScreenEvent.OnSearchIconClicked)
                            }
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        CategoryTabRow(
                            pagerState = pagerState,
                            categories = categories,
                            onTabSelected = { index ->
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            }
                        )
                        HorizontalPager(
                            state = pagerState,
                        ) {
                            NewsArticleList(
                                state = state,
                                onCardClicked = { article ->
                                    bottomSheetShow = true
                                    onEvent(NewsScreenEvent.OnNewsCardClicked(article = article))
                                },
                                onRetry = {
                                    onEvent(NewsScreenEvent.OnCategoryChanged(state.category))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsArticleList(
    state: NewsScreenState,
    onCardClicked: (Article) -> Unit,
    onRetry: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(state.articles) { article ->
            NewArticleCard(
                article = article,
                onCardClick = onCardClicked
            )
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        }
        if (state.error != null) {
            RetryContent(error = state.error, onRetry = onRetry)
        }
        if (state.articles.isEmpty() && state.error == null && !state.isLoading) {
            Text(text = "No Data To Show")
        }
    }
}