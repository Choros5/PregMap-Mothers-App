package com.elvis.pregmap.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import kotlinx.coroutines.delay
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

data class WisdomQuote(
    val english: String,
    val swahili: String,
    val author: String,
    val imageUrl: String
)

data class WellnessTopic(
    val title: String,
    val description: String,
    val imageUrl: String,
    val trimester: Int
)

data class Milestone(
    val title: String,
    val description: String,
    val week: Int,
    val isCompleted: Boolean = false
)

data class PregnancyJourney(
    val currentWeek: Int,
    val babySize: String,
    val babySizeImage: String,
    val nextVisit: String,
    val symptoms: List<String>
)

@Composable
fun HomeScreen() {
    // Sample data
    val pregnancyJourney = remember {
        PregnancyJourney(
            currentWeek = 24,
            babySize = "Corn",
            babySizeImage = "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&h=300&fit=crop",
            nextVisit = "Next ANC Visit: March 15, 2024",
            symptoms = listOf("Mild back pain", "Increased appetite", "Baby movements")
        )
    }
    
    val wisdomQuotes = remember {
        listOf(
            WisdomQuote(
                english = "A mother's love is the fuel that enables a normal human being to do the impossible.",
                swahili = "Upendo wa mama ni mafuta yanayowezesha mtu wa kawaida kufanya isiyowezekana.",
                author = "Marion C. Garretty",
                imageUrl = "https://images.pexels.com/photos/415824/pexels-photo-415824.jpeg?auto=compress&w=800&h=400&fit=crop"
            ),
            WisdomQuote(
                english = "The moment a child is born, the mother is also born. She never existed before.",
                swahili = "Wakati mtoto anapozaliwa, mama pia anazaliwa. Hakuwahi kuwepo hapo awali.",
                author = "Rajneesh",
                imageUrl = "https://images.pexels.com/photos/377058/pexels-photo-377058.jpeg?auto=compress&w=800&h=400&fit=crop"
            ),
            WisdomQuote(
                english = "Motherhood is the greatest thing and the hardest thing.",
                swahili = "Uzazi ni jambo kubwa zaidi na jambo gumu zaidi.",
                author = "Ricki Lake",
                imageUrl = "https://images.pexels.com/photos/302083/pexels-photo-302083.jpeg?auto=compress&w=800&h=400&fit=crop"
            )
        )
    }
    
    val wellnessTopics = remember {
        listOf(
            WellnessTopic(
                title = "Nutrition & Diet",
                description = "Essential nutrients for you and your baby",
                imageUrl = "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=400&h=300&fit=crop",
                trimester = 2
            ),
            WellnessTopic(
                title = "Exercise & Movement",
                description = "Safe exercises for each trimester",
                imageUrl = "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&h=300&fit=crop",
                trimester = 2
            ),
            WellnessTopic(
                title = "Mental Health",
                description = "Managing stress and emotional well-being",
                imageUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop",
                trimester = 2
            )
        )
    }
    
    val milestones = remember {
        listOf(
            Milestone("First Heartbeat", "Baby's heart starts beating", 6, true),
            Milestone("First Movement", "You feel baby's first kicks", 18, true),
            Milestone("Gender Reveal", "Find out if it's a boy or girl", 20, true),
            Milestone("Viability Milestone", "Baby can survive outside womb", 24, false),
            Milestone("Third Trimester", "Final stretch begins", 28, false),
            Milestone("Full Term", "Baby is ready to be born", 37, false)
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item {
            DynamicWelcomeBanner(pregnancyJourney)
        }
        
        item {
            WisdomQuoteCarousel(wisdomQuotes)
        }
        
        item {
            WellnessCardsSection(wellnessTopics)
        }
        
        item {
            InteractiveTimeline(milestones)
        }
        
        item {
            PregnancyJourneyCard(pregnancyJourney)
        }
    }
}

@Composable
fun DynamicWelcomeBanner(pregnancyJourney: PregnancyJourney) {
    var isAnimating by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isAnimating = true
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pregnancyJourney.babySizeImage)
                    .crossfade(true)
                    .build(),
                contentDescription = "Baby size comparison",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedVisibility(
                    visible = isAnimating,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(1000)
                    ) + fadeIn(animationSpec = tween(1000))
                ) {
                    Text(
                        text = "Week ${pregnancyJourney.currentWeek}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                AnimatedVisibility(
                    visible = isAnimating,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(1000, delayMillis = 500)
                    ) + fadeIn(animationSpec = tween(1000, delayMillis = 500))
                ) {
                    Column {
                        Text(
                            text = "Your baby is the size of a",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                        Text(
                            text = pregnancyJourney.babySize,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, com.google.accompanist.pager.ExperimentalPagerApi::class)
@Composable
fun WisdomQuoteCarousel(quotes: List<WisdomQuote>) {
    val pagerState = rememberPagerState()

    // Auto-scroll the pager
    LaunchedEffect(Unit) {
        while (true) {
            delay(6000)
            pagerState.animateScrollToPage(
                page = (pagerState.currentPage + 1) % quotes.size,
                animationSpec = tween(1000)
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        HorizontalPager(
            count = quotes.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val quote = quotes[page]
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(quote.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Quote background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay for better text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Black.copy(alpha = 0.8f)
                                )
                            )
                        )
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Quote",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Quote text
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = quote.english,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 24.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "- ${quote.author}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Page indicators
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(quotes.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == pagerState.currentPage) 9.dp else 7.dp)
                                        .background(
                                            color = if (index == pagerState.currentPage)
                                                Color.White else Color.White.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WellnessCardsSection(topics: List<WellnessTopic>) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(4000L)
            if (lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
                val nextIndex = lazyListState.firstVisibleItemIndex + 1
                lazyListState.animateScrollToItem(index = nextIndex)
            }
        }
    }
    
    Column {
        Text(
            text = "Wellness Topics",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(Int.MAX_VALUE) { index ->
                val topicIndex = index % topics.size
                WellnessCard(topics[topicIndex])
            }
        }
    }
}

@Composable
fun WellnessCard(topic: WellnessTopic) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(150.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(topic.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = topic.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = topic.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )
            }
        }
    }
}

@Composable
fun InteractiveTimeline(milestones: List<Milestone>) {
    Column {
        Text(
            text = "Pregnancy Timeline",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(milestones) { milestone ->
                TimelineCard(milestone)
            }
        }
    }
}

@Composable
fun TimelineCard(milestone: Milestone) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (milestone.isCompleted) 
                Color(0xFF4CAF50) else Color(0xFFE0E0E0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Week ${milestone.week}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (milestone.isCompleted) Color.White else Color(0xFF424242),
                        fontWeight = FontWeight.Bold
                    )
                )
                if (milestone.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = if (milestone.isCompleted) Color.White else Color(0xFF424242),
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = milestone.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (milestone.isCompleted) 
                            Color.White.copy(alpha = 0.9f) else Color(0xFF666666)
                    )
                )
            }
        }
    }
}

@Composable
fun PregnancyJourneyCard(journey: PregnancyJourney) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pregnancy Journey",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                )
                IconButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF1976D2)
                    )
                }
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Next ANC Visit",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                        )
                        Text(
                            text = journey.nextVisit,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF424242)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3E5F5)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Current Symptoms",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7B1FA2)
                            )
                        )
                        journey.symptoms.forEach { symptom ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF7B1FA2), shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = symptom,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF424242)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 