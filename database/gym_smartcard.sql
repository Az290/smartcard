-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th10 30, 2025 lúc 03:31 AM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `gym_smartcard`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `checkins`
--

CREATE TABLE `checkins` (
  `id` int(11) NOT NULL,
  `member_id` int(11) NOT NULL,
  `checkin_time` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `members`
--

CREATE TABLE `members` (
  `id` int(11) NOT NULL,
  `card_id` varchar(20) NOT NULL,
  `name_enc` text NOT NULL,
  `phone_enc` longtext NOT NULL,
  `phone_hash` varchar(64) NOT NULL,
  `birth_date_enc` longtext DEFAULT NULL,
  `avatar_path` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `balance` bigint(20) DEFAULT 0,
  `status` varchar(10) DEFAULT 'active'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `members`
--

INSERT INTO `members` (`id`, `card_id`, `name_enc`, `phone_enc`, `phone_hash`, `birth_date_enc`, `avatar_path`, `created_at`, `balance`, `status`) VALUES
(1, 'GYM3389', 'Vpie7lS/I/ukI/vg5SUSHQ==', 'Td2dg0vppvsd0lgsALDeZg==', '8df8d54cf22ee612623fea9f1d99d622b1c0b04bd34e40b676e65305d07e0243', NULL, NULL, '2025-11-30 09:01:43', 0, 'active');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `member_packages`
--

CREATE TABLE `member_packages` (
  `id` int(11) NOT NULL,
  `member_id` int(11) NOT NULL,
  `package_id` tinyint(4) NOT NULL,
  `trainer_id` tinyint(4) DEFAULT NULL,
  `buy_date` datetime DEFAULT current_timestamp(),
  `expire_date` datetime DEFAULT NULL,
  `remaining_sessions` smallint(6) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `packages`
--

CREATE TABLE `packages` (
  `id` tinyint(4) NOT NULL,
  `name` varchar(100) NOT NULL,
  `price` int(11) NOT NULL,
  `duration_days` smallint(6) DEFAULT NULL,
  `sessions` smallint(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `packages`
--

INSERT INTO `packages` (`id`, `name`, `price`, `duration_days`, `sessions`, `description`) VALUES
(1, 'Gói 1 tháng', 800000, 30, NULL, 'Tập không giới hạn 30 ngày'),
(2, 'Gói 3 tháng', 2200000, 90, NULL, 'Tiết kiệm 10%'),
(3, 'Gói 6 tháng', 4000000, 180, NULL, 'Tiết kiệm 16%'),
(4, 'PT 10 buổi', 0, NULL, 10, 'Huấn luyện viên cá nhân'),
(5, 'PT 20 buổi', 0, NULL, 20, 'Huấn luyện viên cá nhân');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `trainers`
--

CREATE TABLE `trainers` (
  `id` tinyint(4) NOT NULL,
  `name` varchar(100) NOT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `experience_years` tinyint(4) NOT NULL,
  `rating` decimal(2,1) DEFAULT 5.0,
  `avatar_path` varchar(255) DEFAULT NULL,
  `bio` text DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `trainers`
--

INSERT INTO `trainers` (`id`, `name`, `phone`, `experience_years`, `rating`, `avatar_path`, `bio`, `is_active`) VALUES
(1, 'Nguyễn Văn Hùng', '0901234567', 8, 4.9, NULL, 'Chuyên gia bodybuilding, 3 lần vô địch tỉnh', 1),
(2, 'Trần Thị Mỹ Linh', '0912345678', 5, 5.0, NULL, 'Chuyên yoga & giảm mỡ, hotgirl gym nổi tiếng', 1),
(3, 'Lê Hoàng Anh', '0923456789', 12, 4.8, NULL, 'HLV đội tuyển thể hình VN 2018-2022', 1),
(4, 'Phạm Minh Tuấn', '0934567890', 3, 4.7, NULL, 'Chuyên giảm cân cấp tốc cho người mới', 1),
(5, 'Vũ Ngọc Ánh', '0945678901', 6, 4.9, NULL, 'Chuyên nữ - body chuẩn bikini', 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `trainer_prices`
--

CREATE TABLE `trainer_prices` (
  `id` int(11) NOT NULL,
  `trainer_id` tinyint(4) NOT NULL,
  `package_type` enum('SESSION_10','SESSION_20','SESSION_50','MONTHLY_UNLIMITED') NOT NULL,
  `price` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `trainer_prices`
--

INSERT INTO `trainer_prices` (`id`, `trainer_id`, `package_type`, `price`) VALUES
(1, 1, 'SESSION_10', 3500000),
(2, 1, 'SESSION_20', 6500000),
(3, 2, 'SESSION_10', 3200000),
(4, 2, 'SESSION_20', 6000000),
(5, 3, 'SESSION_10', 3000000),
(6, 3, 'SESSION_20', 5500000),
(7, 4, 'SESSION_10', 2500000),
(8, 4, 'SESSION_20', 4500000),
(9, 5, 'SESSION_10', 2800000),
(10, 5, 'SESSION_20', 5200000);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `transactions`
--

CREATE TABLE `transactions` (
  `id` int(11) NOT NULL,
  `member_id` int(11) NOT NULL,
  `type` enum('TOPUP','BUY_PACKAGE') NOT NULL,
  `amount` int(11) NOT NULL,
  `package_id` tinyint(4) DEFAULT NULL,
  `trainer_id` tinyint(4) DEFAULT NULL,
  `signature` text DEFAULT NULL,
  `trans_time` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `checkins`
--
ALTER TABLE `checkins`
  ADD PRIMARY KEY (`id`),
  ADD KEY `member_id` (`member_id`);

--
-- Chỉ mục cho bảng `members`
--
ALTER TABLE `members`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `card_uid` (`card_id`),
  ADD UNIQUE KEY `card_id` (`card_id`),
  ADD KEY `idx_phone_hash` (`phone_hash`);

--
-- Chỉ mục cho bảng `member_packages`
--
ALTER TABLE `member_packages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `member_id` (`member_id`),
  ADD KEY `package_id` (`package_id`),
  ADD KEY `trainer_id` (`trainer_id`);

--
-- Chỉ mục cho bảng `packages`
--
ALTER TABLE `packages`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `trainers`
--
ALTER TABLE `trainers`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `trainer_prices`
--
ALTER TABLE `trainer_prices`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_trainer_package` (`trainer_id`,`package_type`);

--
-- Chỉ mục cho bảng `transactions`
--
ALTER TABLE `transactions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `member_id` (`member_id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `checkins`
--
ALTER TABLE `checkins`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `members`
--
ALTER TABLE `members`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT cho bảng `member_packages`
--
ALTER TABLE `member_packages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `packages`
--
ALTER TABLE `packages`
  MODIFY `id` tinyint(4) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT cho bảng `trainers`
--
ALTER TABLE `trainers`
  MODIFY `id` tinyint(4) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT cho bảng `trainer_prices`
--
ALTER TABLE `trainer_prices`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT cho bảng `transactions`
--
ALTER TABLE `transactions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `checkins`
--
ALTER TABLE `checkins`
  ADD CONSTRAINT `checkins_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`);

--
-- Các ràng buộc cho bảng `member_packages`
--
ALTER TABLE `member_packages`
  ADD CONSTRAINT `member_packages_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `member_packages_ibfk_2` FOREIGN KEY (`package_id`) REFERENCES `packages` (`id`),
  ADD CONSTRAINT `member_packages_ibfk_3` FOREIGN KEY (`trainer_id`) REFERENCES `trainers` (`id`);

--
-- Các ràng buộc cho bảng `trainer_prices`
--
ALTER TABLE `trainer_prices`
  ADD CONSTRAINT `trainer_prices_ibfk_1` FOREIGN KEY (`trainer_id`) REFERENCES `trainers` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `transactions`
--
ALTER TABLE `transactions`
  ADD CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
