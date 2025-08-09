package com.pucetec.expensemate.models.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "movements")
data class Movement(
    @Enumerated(EnumType.STRING)
    var type: MovementType,

    var amount: Double,

    var date: LocalDateTime = LocalDateTime.now(),

    var note: String? = null,

    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne
    @JoinColumn(name = "category_id")
    var category: Category,

    @ManyToOne
    @JoinColumn(name = "credit_card_id")
    var creditCard: CreditCard? = null,

    @ManyToOne
    @JoinColumn(name = "account_id")
    var account: Account? = null
) : BaseEntity()
