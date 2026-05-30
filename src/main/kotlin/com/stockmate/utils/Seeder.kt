package com.stockmate.utils

import com.stockmate.models.tables.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.math.BigDecimal
import java.time.LocalDateTime

object Seeder {

    fun seed() {
        val hasUsers = transaction { Users.selectAll().empty() }
        if (!hasUsers) return

        transaction {
            // === TEST ACCOUNTS ===
            val adminId = Users.insertAndGetId {
                it[email] = "admin@stockmate.ru"
                it[passwordHash] = BCrypt.hashpw("admin123", BCrypt.gensalt(12))
                it[fullName] = "Иванов Иван"
                it[role] = "Admin"
                it[isActive] = true
                it[createdAt] = LocalDateTime.now()
            }

            val managerId = Users.insertAndGetId {
                it[email] = "manager@stockmate.ru"
                it[passwordHash] = BCrypt.hashpw("manager123", BCrypt.gensalt(12))
                it[fullName] = "Петрова Мария"
                it[role] = "Manager"
                it[isActive] = true
                it[createdAt] = LocalDateTime.now()
            }

            val warehouseId1 = Users.insertAndGetId {
                it[email] = "warehouse@stockmate.ru"
                it[passwordHash] = BCrypt.hashpw("warehouse123", BCrypt.gensalt(12))
                it[fullName] = "Сидоров Алексей"
                it[role] = "Warehouse"
                it[isActive] = true
                it[createdAt] = LocalDateTime.now()
            }

            val warehouseId2 = Users.insertAndGetId {
                it[email] = "warehouse2@stockmate.ru"
                it[passwordHash] = BCrypt.hashpw("warehouse123", BCrypt.gensalt(12))
                it[fullName] = "Козлова Дарья"
                it[role] = "Warehouse"
                it[isActive] = true
                it[createdAt] = LocalDateTime.now()
            }

            val analystId = Users.insertAndGetId {
                it[email] = "analyst@stockmate.ru"
                it[passwordHash] = BCrypt.hashpw("analyst123", BCrypt.gensalt(12))
                it[fullName] = "Новиков Дмитрий"
                it[role] = "Analyst"
                it[isActive] = true
                it[createdAt] = LocalDateTime.now()
            }

            // === UNITS ===
            val unitPcs = Units.insertAndGetId { it[name] = "Штука"; it[shortName] = "шт" }
            val unitKg = Units.insertAndGetId { it[name] = "Килограмм"; it[shortName] = "кг" }
            val unitL = Units.insertAndGetId { it[name] = "Литр"; it[shortName] = "л" }
            val unitM = Units.insertAndGetId { it[name] = "Метр"; it[shortName] = "м" }
            val unitPack = Units.insertAndGetId { it[name] = "Упаковка"; it[shortName] = "уп" }

            // === CATEGORIES ===
            val catElectronics = Categories.insertAndGetId { it[name] = "Электроника" }
            val catFurniture = Categories.insertAndGetId { it[name] = "Мебель" }
            val catTools = Categories.insertAndGetId { it[name] = "Инструменты" }
            val catMaterials = Categories.insertAndGetId { it[name] = "Материалы" }
            val catOffice = Categories.insertAndGetId { it[name] = "Канцелярия" }
            val catCables = Categories.insertAndGetId { it[name] = "Кабели"; it[parentId] = catElectronics }
            val catComponents = Categories.insertAndGetId { it[name] = "Комплектующие"; it[parentId] = catElectronics }

            // === WAREHOUSES ===
            val whMain = Warehouses.insertAndGetId {
                it[name] = "Основной склад"
                it[Warehouses.address] = "г. Москва, ул. Промышленная, д. 15"
                it[Warehouses.managerId] = warehouseId1.value
                it[isActive] = true
            }
            val whSecond = Warehouses.insertAndGetId {
                it[name] = "Склад №2"
                it[Warehouses.address] = "г. Москва, ул. Заводская, д. 8"
                it[Warehouses.managerId] = warehouseId2.value
                it[isActive] = true
            }

            // === ZONES & CELLS ===
            val zoneA = Zones.insertAndGetId { it[warehouseId] = whMain; it[name] = "Зона A" }
            val zoneB = Zones.insertAndGetId { it[warehouseId] = whMain; it[name] = "Зона B" }
            val zoneC = Zones.insertAndGetId { it[warehouseId] = whSecond; it[name] = "Зона C" }

            Cells.insert { it[zoneId] = zoneA; it[code] = "A-01-01" }
            Cells.insert { it[zoneId] = zoneA; it[code] = "A-01-02" }
            Cells.insert { it[zoneId] = zoneA; it[code] = "A-02-01" }
            Cells.insert { it[zoneId] = zoneB; it[code] = "B-01-01" }
            Cells.insert { it[zoneId] = zoneB; it[code] = "B-02-01" }
            Cells.insert { it[zoneId] = zoneC; it[code] = "C-01-01" }
            Cells.insert { it[zoneId] = zoneC; it[code] = "C-02-01" }

            // === PRODUCTS ===
            val p1 = Products.insertAndGetId {
                it[sku] = "EL-001"; it[name] = "Кабель HDMI 2м"; it[barcode] = "4601234567001"
                it[categoryId] = catCables; it[unitId] = unitPcs
                it[minStock] = BigDecimal(50); it[purchasePrice] = BigDecimal(120)
                it[sellPrice] = BigDecimal(250); it[createdAt] = LocalDateTime.now()
            }
            val p2 = Products.insertAndGetId {
                it[sku] = "EL-002"; it[name] = "Кабель USB-C 1м"; it[barcode] = "4601234567002"
                it[categoryId] = catCables; it[unitId] = unitPcs
                it[minStock] = BigDecimal(100); it[purchasePrice] = BigDecimal(80)
                it[sellPrice] = BigDecimal(190); it[createdAt] = LocalDateTime.now()
            }
            val p3 = Products.insertAndGetId {
                it[sku] = "EL-003"; it[name] = "SSD 256GB"; it[barcode] = "4601234567003"
                it[categoryId] = catComponents; it[unitId] = unitPcs
                it[minStock] = BigDecimal(20); it[purchasePrice] = BigDecimal(1800)
                it[sellPrice] = BigDecimal(3200); it[createdAt] = LocalDateTime.now()
            }
            val p4 = Products.insertAndGetId {
                it[sku] = "EL-004"; it[name] = "Модуль памяти DDR4 8GB"; it[barcode] = "4601234567004"
                it[categoryId] = catComponents; it[unitId] = unitPcs
                it[minStock] = BigDecimal(30); it[purchasePrice] = BigDecimal(1500)
                it[sellPrice] = BigDecimal(2800); it[createdAt] = LocalDateTime.now()
            }
            val p5 = Products.insertAndGetId {
                it[sku] = "FU-001"; it[name] = "Стол офисный 120x60"; it[barcode] = "4601234567005"
                it[categoryId] = catFurniture; it[unitId] = unitPcs
                it[minStock] = BigDecimal(5); it[purchasePrice] = BigDecimal(4500)
                it[sellPrice] = BigDecimal(7900); it[createdAt] = LocalDateTime.now()
            }
            val p6 = Products.insertAndGetId {
                it[sku] = "FU-002"; it[name] = "Стул офисный"; it[barcode] = "4601234567006"
                it[categoryId] = catFurniture; it[unitId] = unitPcs
                it[minStock] = BigDecimal(10); it[purchasePrice] = BigDecimal(2800)
                it[sellPrice] = BigDecimal(5200); it[createdAt] = LocalDateTime.now()
            }
            val p7 = Products.insertAndGetId {
                it[sku] = "TL-001"; it[name] = "Набор отвёрток 12шт"; it[barcode] = "4601234567007"
                it[categoryId] = catTools; it[unitId] = unitPack
                it[minStock] = BigDecimal(15); it[purchasePrice] = BigDecimal(600)
                it[sellPrice] = BigDecimal(1100); it[createdAt] = LocalDateTime.now()
            }
            val p8 = Products.insertAndGetId {
                it[sku] = "TL-002"; it[name] = "Паяльная станция"; it[barcode] = "4601234567008"
                it[categoryId] = catTools; it[unitId] = unitPcs
                it[minStock] = BigDecimal(3); it[purchasePrice] = BigDecimal(3500)
                it[sellPrice] = BigDecimal(6800); it[createdAt] = LocalDateTime.now()
            }
            val p9 = Products.insertAndGetId {
                it[sku] = "MA-001"; it[name] = "Термоклей стержни 200шт"; it[barcode] = "4601234567009"
                it[categoryId] = catMaterials; it[unitId] = unitPack
                it[minStock] = BigDecimal(40); it[purchasePrice] = BigDecimal(150)
                it[sellPrice] = BigDecimal(350); it[createdAt] = LocalDateTime.now()
            }
            val p10 = Products.insertAndGetId {
                it[sku] = "OF-001"; it[name] = "Бумага А4 500л"; it[barcode] = "4601234567010"
                it[categoryId] = catOffice; it[unitId] = unitPack
                it[minStock] = BigDecimal(20); it[purchasePrice] = BigDecimal(250)
                it[sellPrice] = BigDecimal(450); it[createdAt] = LocalDateTime.now()
            }
            val p11 = Products.insertAndGetId {
                it[sku] = "MA-002"; it[name] = "Изолента синяя 20м"; it[barcode] = "4601234567011"
                it[categoryId] = catMaterials; it[unitId] = unitPcs
                it[minStock] = BigDecimal(200); it[purchasePrice] = BigDecimal(30)
                it[sellPrice] = BigDecimal(65); it[createdAt] = LocalDateTime.now()
            }
            val p12 = Products.insertAndGetId {
                it[sku] = "EL-005"; it[name] = "Кабель Ethernet 5м"; it[barcode] = "4601234567012"
                it[categoryId] = catCables; it[unitId] = unitPcs
                it[minStock] = BigDecimal(80); it[purchasePrice] = BigDecimal(90)
                it[sellPrice] = BigDecimal(200); it[createdAt] = LocalDateTime.now()
            }

            // === COUNTERPARTIES ===
            val cp1 = Counterparties.insertAndGetId {
                it[name] = "ООО «ТехноСнаб»"; it[inn] = "7712345678"
                it[contactName] = "Смирнов Олег"; it[phone] = "+74951234567"
                it[email] = "info@technosnab.ru"; it[type] = "SUPPLIER"
            }
            val cp2 = Counterparties.insertAndGetId {
                it[name] = "ИП Волков А.С."; it[inn] = "501234567890"
                it[contactName] = "Волков Андрей"; it[phone] = "+79161234567"
                it[email] = "volkov@mail.ru"; it[type] = "SUPPLIER"
            }
            val cp3 = Counterparties.insertAndGetId {
                it[name] = "ООО «ОфисЛайн»"; it[inn] = "7798765432"
                it[contactName] = "Белова Наталья"; it[phone] = "+74957654321"
                it[email] = "order@officeline.ru"; it[type] = "BUYER"
            }

            // === STOCK ===
            Stock.insert { it[productId] = p1; it[warehouseId] = whMain; it[quantity] = BigDecimal(120); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p2; it[warehouseId] = whMain; it[quantity] = BigDecimal(200); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p3; it[warehouseId] = whMain; it[quantity] = BigDecimal(35); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p4; it[warehouseId] = whMain; it[quantity] = BigDecimal(18); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p5; it[warehouseId] = whMain; it[quantity] = BigDecimal(8); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p6; it[warehouseId] = whMain; it[quantity] = BigDecimal(3); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p7; it[warehouseId] = whMain; it[quantity] = BigDecimal(45); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p8; it[warehouseId] = whMain; it[quantity] = BigDecimal(2); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p9; it[warehouseId] = whMain; it[quantity] = BigDecimal(60); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p10; it[warehouseId] = whMain; it[quantity] = BigDecimal(25); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p11; it[warehouseId] = whMain; it[quantity] = BigDecimal(150); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p12; it[warehouseId] = whMain; it[quantity] = BigDecimal(90); it[updatedAt] = LocalDateTime.now() }

            Stock.insert { it[productId] = p1; it[warehouseId] = whSecond; it[quantity] = BigDecimal(30); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p2; it[warehouseId] = whSecond; it[quantity] = BigDecimal(50); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p3; it[warehouseId] = whSecond; it[quantity] = BigDecimal(10); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p7; it[warehouseId] = whSecond; it[quantity] = BigDecimal(12); it[updatedAt] = LocalDateTime.now() }
            Stock.insert { it[productId] = p10; it[warehouseId] = whSecond; it[quantity] = BigDecimal(15); it[updatedAt] = LocalDateTime.now() }

            // === DOCUMENTS (receipt example) ===
            val doc1 = Documents.insertAndGetId {
                it[type] = "RECEIPT"; it[status] = "CONDUCTED"
                it[warehouseToId] = whMain; it[counterpartyId] = cp1
                it[createdBy] = warehouseId1; it[createdAt] = LocalDateTime.now().minusDays(5)
                it[conductedAt] = LocalDateTime.now().minusDays(5)
                it[comment] = "Поступление кабельной продукции"
            }
            DocumentItems.insert { it[documentId] = doc1; it[productId] = p1; it[quantity] = BigDecimal(100); it[price] = BigDecimal(120) }
            DocumentItems.insert { it[documentId] = doc1; it[productId] = p2; it[quantity] = BigDecimal(150); it[price] = BigDecimal(80) }
            DocumentItems.insert { it[documentId] = doc1; it[productId] = p12; it[quantity] = BigDecimal(80); it[price] = BigDecimal(90) }

            val doc2 = Documents.insertAndGetId {
                it[type] = "RECEIPT"; it[status] = "CONDUCTED"
                it[warehouseToId] = whMain; it[counterpartyId] = cp2
                it[createdBy] = warehouseId1; it[createdAt] = LocalDateTime.now().minusDays(3)
                it[conductedAt] = LocalDateTime.now().minusDays(3)
                it[comment] = "Поступление инструментов"
            }
            DocumentItems.insert { it[documentId] = doc2; it[productId] = p7; it[quantity] = BigDecimal(40); it[price] = BigDecimal(600) }
            DocumentItems.insert { it[documentId] = doc2; it[productId] = p8; it[quantity] = BigDecimal(5); it[price] = BigDecimal(3500) }

            val doc3 = Documents.insertAndGetId {
                it[type] = "SHIPMENT"; it[status] = "CONDUCTED"
                it[warehouseFromId] = whMain; it[counterpartyId] = cp3
                it[createdBy] = warehouseId1; it[createdAt] = LocalDateTime.now().minusDays(1)
                it[conductedAt] = LocalDateTime.now().minusDays(1)
                it[comment] = "Отгрузка офисной мебели"
            }
            DocumentItems.insert { it[documentId] = doc3; it[productId] = p5; it[quantity] = BigDecimal(2); it[price] = BigDecimal(7900) }
            DocumentItems.insert { it[documentId] = doc3; it[productId] = p6; it[quantity] = BigDecimal(7); it[price] = BigDecimal(5200) }

            // Draft document
            val doc4 = Documents.insertAndGetId {
                it[type] = "RECEIPT"; it[status] = "DRAFT"
                it[warehouseToId] = whMain; it[counterpartyId] = cp1
                it[createdBy] = warehouseId1; it[createdAt] = LocalDateTime.now()
                it[comment] = "Ожидаемая поставка комплектующих"
            }
            DocumentItems.insert { it[documentId] = doc4; it[productId] = p3; it[quantity] = BigDecimal(20); it[price] = BigDecimal(1800) }
            DocumentItems.insert { it[documentId] = doc4; it[productId] = p4; it[quantity] = BigDecimal(25); it[price] = BigDecimal(1500) }

            // === NOTIFICATIONS ===
            Notifications.insert {
                it[userId] = warehouseId1.value; it[type] = "LOW_STOCK"
                it[title] = "Низкий остаток"; it[body] = "Стул офисный — остаток ниже минимума (3 из 10)"
                it[entityType] = "product"; it[entityId] = p6.value
                it[isRead] = false; it[createdAt] = LocalDateTime.now().minusHours(2)
            }
            Notifications.insert {
                it[userId] = warehouseId1.value; it[type] = "LOW_STOCK"
                it[title] = "Низкий остаток"; it[body] = "Паяльная станция — остаток ниже минимума (2 из 3)"
                it[entityType] = "product"; it[entityId] = p8.value
                it[isRead] = false; it[createdAt] = LocalDateTime.now().minusHours(1)
            }
            Notifications.insert {
                it[userId] = adminId.value; it[type] = "REQUEST"
                it[title] = "Новая заявка на пополнение"; it[body] = "Запрос на модули памяти DDR4, 30 шт"
                it[entityType] = "product"; it[entityId] = p4.value
                it[isRead] = true; it[createdAt] = LocalDateTime.now().minusDays(1)
            }

            // === REPLENISHMENT REQUESTS ===
            ReplenishmentRequests.insert {
                it[productId] = p4; it[warehouseId] = whMain
                it[quantity] = BigDecimal(30); it[status] = "NEW"
                it[createdBy] = managerId; it[assignedTo] = null
                it[comment] = "Срочно нужно пополнить, остаток критический"
                it[createdAt] = LocalDateTime.now().minusHours(3)
            }
            ReplenishmentRequests.insert {
                it[productId] = p6; it[warehouseId] = whMain
                it[quantity] = BigDecimal(15); it[status] = "IN_PROGRESS"
                it[createdBy] = managerId; it[assignedTo] = warehouseId1
                it[comment] = null; it[createdAt] = LocalDateTime.now().minusDays(2)
            }
            ReplenishmentRequests.insert {
                it[productId] = p8; it[warehouseId] = whMain
                it[quantity] = BigDecimal(5); it[status] = "DONE"
                it[createdBy] = managerId; it[assignedTo] = warehouseId1
                it[comment] = null; it[createdAt] = LocalDateTime.now().minusDays(7)
            }
        }
    }
}
