package com.nfccard

import com.nfccard.data.BusinessCard
import com.nfccard.data.nfcPayloadToCard
import com.nfccard.data.toNfcPayload
import org.junit.Assert.*
import org.junit.Test

class CardSerializationTest {

    private val sampleCard = BusinessCard(
        id = "test-1",
        name = "홍길동",
        title = "선임 엔지니어",
        company = "삼성전자",
        phone = "010-1234-5678",
        email = "hong@samsung.com",
        website = "https://samsung.com",
        colorHex = "#00F5C4"
    )

    @Test
    fun `NFC 페이로드 직렬화 후 역직렬화 검증`() {
        val payload = sampleCard.toNfcPayload()
        assertFalse("페이로드가 비어있으면 안됨", payload.isEmpty())
        assertTrue("JSON 형식이어야 함", payload.startsWith("{"))

        val restored = nfcPayloadToCard(payload)
        assertEquals("이름 일치", sampleCard.name, restored.name)
        assertEquals("직함 일치", sampleCard.title, restored.title)
        assertEquals("회사 일치", sampleCard.company, restored.company)
        assertEquals("전화번호 일치", sampleCard.phone, restored.phone)
        assertEquals("이메일 일치", sampleCard.email, restored.email)
        assertEquals("웹사이트 일치", sampleCard.website, restored.website)
        assertEquals("색상 일치", sampleCard.colorHex, restored.colorHex)
    }

    @Test
    fun `빈 웹사이트도 직렬화 가능`() {
        val card = sampleCard.copy(website = "")
        val payload = card.toNfcPayload()
        val restored = nfcPayloadToCard(payload)
        assertEquals("빈 웹사이트 유지", "", restored.website)
    }

    @Test
    fun `페이로드 크기 NFC 제한 이하 확인 (최대 1KB)`() {
        val payload = sampleCard.toNfcPayload()
        assertTrue("페이로드 1KB 이하여야 함", payload.toByteArray(Charsets.UTF_8).size < 1024)
    }

    @Test
    fun `잘못된 JSON 파싱 시 기본값 반환`() {
        val badPayload = """{"n":"홍길동"}"""
        val card = nfcPayloadToCard(badPayload)
        assertEquals("홍길동", card.name)
        assertEquals("", card.title)
        assertEquals("#00F5C4", card.colorHex)  // 기본 색상
    }

    @Test
    fun `이름 필수 필드 검증`() {
        val card = BusinessCard()
        assertTrue("기본 이름은 빈 문자열", card.name.isEmpty())
    }

    @Test
    fun `ID는 고유해야 함`() {
        val card1 = BusinessCard()
        Thread.sleep(1)
        val card2 = BusinessCard()
        assertNotEquals("ID 중복 불가", card1.id, card2.id)
    }
}
