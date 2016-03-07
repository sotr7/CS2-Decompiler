/*
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import mgi.tools.jagdecs2.CS2Decompiler;
import mgi.tools.jagdecs2.CS2Type;
import mgi.tools.jagdecs2.ast.FunctionNode;
import mgi.tools.jagdecs2.util.ConfigsDatabase;
import mgi.tools.jagdecs2.util.FunctionDatabase;
import mgi.tools.jagdecs2.util.FunctionInfo;
import mgi.tools.jagdecs2.util.InstructionInfo;
import mgi.tools.jagdecs2.util.InstructionsDatabase;

public class GenDatabases {
	
	static int[] order;
	static String[] nameso, names;
	
	static {
		order = new int[] { 750, 422, 506, 1076, 410, 1027, 376, 580, 1125, 231, 1228, 811, 279, 836, 868, 863, 871, 475, 887, 132, 159, 1203, 205, 543, 483, 357, 1149, 671, 335, 108, 926, 188, 253, 1139, 1047, 736, 980, 121, 325, 225, 812, 1002, 873, 1059, 1178, 956, 127, 1035, 233, 1246, 424, 464, 895, 916, 82, 780, 505, 904, 137, 304, 667, 1225, 829, 749, 1165, 91, 528, 390, 918, 170, 1009, 387, 1172, 1248, 746, 179, 1214, 865, 39, 795, 766, 974, 786, 111, 955, 1205, 1239, 1241, 1200, 492, 348, 1098, 1174, 413, 665, 163, 845, 302, 1166, 604, 371, 676, 718, 827, 923, 1153, 741, 508, 221, 1219, 314, 1124, 652, 1150, 814, 90, 706, 295, 396, 1119, 466, 1028, 24, 257, 315, 627, 490, 403, 238, 51, 196, 689, 1123, 739, 320, 824, 365, 673, 287, 569, 849, 597, 40, 216, 25, 86, 1102, 87, 6, 214, 742, 146, 35, 899, 641, 538, 174, 1078, 798, 443, 119, 1063, 1232, 647, 309, 547, 924, 711, 831, 480, 684, 210, 1170, 254, 880, 118, 337, 1155, 227, 301, 787, 957, 114, 830, 950, 694, 867, 570, 499, 236, 1209, 1108, 839, 757, 472, 283, 1129, 197, 1131, 389, 562, 235, 678, 1052, 672, 102, 249, 264, 825, 206, 589, 730, 632, 300, 358, 67, 1121, 1133, 415, 853, 1036, 688, 675, 461, 368, 816, 1163, 322, 931, 859, 1126, 1145, 71, 10, 902, 903, 869, 861, 541, 1072, 1255, 1247, 1041, 326, 1101, 633, 944, 45, 1197, 953, 925, 484, 622, 215, 581, 436, 598, 507, 783, 760, 535, 603, 913, 1048, 649, 862, 942, 1007, 419, 714, 989, 36, 294, 105, 1106, 1215, 1038, 1224, 707, 805, 519, 982, 669, 496, 715, 893, 450, 20, 1001, 800, 272, 799, 1235, 1171, 964, 576, 844, 134, 407, 481, 420, 386, 963, 1130, 804, 416, 911, 1, 430, 751, 860, 784, 872, 962, 595, 1043, 7, 1061, 1087, 1151, 220, 1217, 958, 280, 14, 275, 534, 1140, 624, 999, 482, 277, 1206, 372, 637, 383, 33, 241, 487, 1143, 1168, 779, 485, 997, 634, 1233, 996, 460, 161, 710, 444, 590, 890, 695, 977, 616, 217, 1032, 209, 398, 308, 579, 463, 1158, 493, 162, 533, 536, 648, 823, 363, 208, 202, 32, 947, 143, 602, 474, 884, 635, 794, 921, 328, 189, 46, 1154, 1068, 588, 1226, 116, 43, 194, 1161, 917, 306, 561, 662, 732, 148, 1021, 611, 834, 433, 723, 1195, 1176, 269, 908, 92, 1026, 530, 889, 666, 910, 888, 155, 151, 651, 374, 185, 1030, 642, 915, 907, 60, 285, 754, 177, 792, 883, 1199, 229, 1104, 397, 983, 645, 837, 657, 514, 1073, 726, 1115, 686, 178, 427, 74, 1054, 807, 479, 230, 1160, 171, 469, 532, 1004, 832, 373, 1250, 1134, 100, 607, 138, 226, 691, 1034, 1113, 858, 207, 1081, 1064, 345, 894, 698, 1085, 1120, 34, 886, 945, 1148, 166, 350, 820, 1014, 1094, 80, 734, 636, 421, 724, 256, 352, 808, 909, 959, 520, 1109, 1074, 182, 545, 1211, 995, 311, 692, 93, 737, 973, 262, 330, 932, 245, 525, 319, 265, 244, 1067, 1253, 712, 401, 610, 168, 85, 1147, 948, 370, 981, 139, 120, 1132, 15, 173, 316, 563, 968, 674, 556, 793, 572, 1079, 631, 620, 539, 891, 729, 2, 1107, 933, 5, 65, 821, 578, 1193, 896, 513, 1025, 361, 453, 38, 575, 1000, 133, 1231, 250, 63, 542, 1105, 323, 343, 713, 1249, 882, 653, 771, 289, 199, 317, 1029, 920, 240, 851, 135, 431, 696, 42, 646, 331, 128, 409, 747, 299, 992, 378, 583, 817, 687, 813, 232, 362, 495, 313, 195, 758, 943, 571, 44, 291, 531, 286, 1018, 1012, 176, 819, 37, 1075, 129, 276, 164, 385, 1156, 1207, 212, 58, 828, 310, 52, 900, 203, 222, 1016, 504, 1089, 537, 606, 97, 755, 1198, 81, 716, 833, 969, 246, 1187, 1062, 554, 874, 147, 271, 928, 856, 428, 759, 104, 1251, 552, 445, 136, 400, 961, 355, 702, 417, 937, 103, 826, 48, 26, 502, 192, 489, 165, 435, 1024, 559, 796, 13, 1103, 1245, 334, 717, 1039, 1050, 1175, 1227, 664, 818, 53, 353, 19, 8, 125, 1071, 455, 1095, 298, 31, 404, 61, 344, 1099, 356, 451, 892, 414, 251, 1114, 1058, 1056, 497, 949, 803, 360, 50, 685, 929, 719, 1049, 237, 587, 379, 1116, 619, 941, 1238, 442, 1189, 340, 654, 72, 501, 660, 412, 677, 986, 1141, 809, 1053, 1122, 234, 521, 333, 1003, 4, 1100, 772, 1242, 494, 609, 126, 975, 142, 69, 110, 1221, 990, 560, 263, 878, 551, 626, 190, 1202, 515, 21, 303, 991, 9, 1186, 366, 1005, 156, 725, 852, 258, 770, 1173, 510, 549, 268, 699, 763, 339, 377, 663, 41, 971, 446, 1033, 978, 940, 425, 1022, 1057, 661, 382, 183, 95, 1237, 838, 441, 752, 1190, 704, 290, 761, 843, 440, 1031, 596, 73, 260, 488, 99, 529, 1051, 1146, 336, 668, 54, 1110, 395, 629, 745, 965, 293, 936, 1083, 1218, 987, 914, 735, 1093, 802, 76, 375, 261, 1042, 1240, 349, 327, 1144, 204, 115, 954, 720, 468, 1196, 211, 1182, 625, 1184, 922, 462, 1040, 332, 150, 273, 1013, 1204, 347, 617, 797, 905, 842, 994, 585, 1015, 857, 388, 1096, 1181, 756, 491, 364, 1188, 1088, 682, 1183, 255, 998, 367, 259, 312, 107, 1017, 89, 351, 613, 1169, 307, 1044, 778, 193, 187, 1090, 270, 1142, 28, 437, 774, 1127, 346, 670, 573, 1254, 399, 605, 591, 708, 523, 767, 722, 972, 1210, 988, 145, 912, 614, 157, 967, 1066, 458, 939, 584, 432, 1159, 709, 731, 140, 381, 870, 548, 934, 785, 78, 705, 1177, 600, 113, 465, 946, 846, 594, 558, 23, 96, 117, 239, 426, 582, 806, 951, 154, 318, 700, 144, 447, 877, 822, 681, 169, 267, 1223, 158, 1023, 359, 278, 0, 1213, 106, 744, 993, 438, 57, 448, 930, 228, 1117, 854, 1152, 1080, 801, 544, 486, 546, 680, 1136, 380, 1208, 457, 775, 1179, 1236, 612, 966, 517, 557, 1086, 733, 840, 112, 1008, 577, 976, 738, 567, 149, 568, 1091, 153, 776, 644, 1216, 423, 927, 1069, 30, 599, 1065, 565, 454, 392, 29, 391, 384, 593, 984, 791, 213, 471, 885, 703, 1167, 329, 354, 456, 77, 553, 16, 1070, 1157, 655, 1192, 66, 284, 1060, 1230, 550, 27, 855, 740, 586, 88, 1194, 777, 84, 898, 11, 1244, 242, 555, 1097, 1229, 1135, 697, 1138, 768, 219, 788, 109, 338, 601, 439, 608, 470, 748, 22, 122, 1006, 56, 864, 985, 841, 62, 592, 17, 1137, 18, 160, 1180, 773, 1045, 1191, 701, 296, 815, 522, 1222, 167, 639, 98, 566, 248, 516, 643, 175, 1118, 1162, 252, 201, 919, 1082, 1092, 124, 1037, 1252, 693, 679, 405, 305, 411, 59, 866, 526, 393, 1201, 518, 478, 282, 123, 765, 324, 342, 191, 1112, 297, 321, 101, 979, 68, 477, 952, 274, 897, 1011, 218, 781, 906, 369, 449, 394, 292, 881, 1084, 500, 810, 727, 1077, 64, 847, 960, 55, 683, 1020, 935, 288, 12, 75, 628, 1220, 540, 638, 452, 131, 152, 511, 408, 1243, 200, 938, 79, 70, 762, 459, 1055, 503, 184, 1010, 850, 186, 198, 1128, 764, 658, 848, 630, 243, 621, 3, 659, 341, 769, 94, 790, 782, 564, 402, 640, 467, 247, 721, 1046, 181, 418, 753, 690, 281, 789, 574, 879, 266, 1164, 875, 1019, 728, 512, 223, 476, 141, 498, 172, 743, 524, 656, 618, 130, 509, 49, 650, 1185, 47, 970, 473, 180, 224, 1111, 429, 901, 434, 615, 527, 835, 406, 876, 1212, 1234, 83, 623 };
		nameso = new String[] { "_push_constant_int", "_push_var", "_pop_var", "_push_varbit", "_pop_varbit", "_push_constant_string", "_branch", "_branch_not", "_branch_equals", "_branch_less_than", "_branch_greater_than", "_branch_less_than_or_equals", "_branch_greater_than_or_equals", "_push_int_local", "_pop_int_local", "_push_string_local", "_pop_string_local", "_join_string", "_pop_int_discard", "_pop_string_discard", "_gosub_with_params", "_define_array", "_push_array_int", "_pop_array_int", "_switch", "_push_long_constant", "_pop_long_discard", "_push_long_local", "_pop_long_local", "_long_branch_not", "_long_branch_equals", "_long_branch_less_than", "_long_branch_greater_than", "_long_branch_less_than_or_equals", "_long_branch_greater_than_or_equals", "_push_array_int_leave_index_on_stack", "_push_array_int_and_index", "_pop_array_int_leave_value_on_stack", "_branch_if_true", "_branch_if_false", "_cc_create", "_cc_delete", "_cc_deleteall", "_cc_invalidate", "_if_sendtofront", "_if_sendtoback", "_cc_sendtofront", "_cc_sendtoback", "_if_resume_pausebutton", "_cc_resume_pausebutton", "_baseidkit", "_basecolour", "_basematerial", "_setgender", "_setobj", "_cc_setposition", "_cc_setsize", "_cc_sethide", "_cc_setaspect", "_cc_setnoclickthrough", "_cc_setscrollpos", "_cc_setcolour", "_cc_setfill", "_cc_settrans", "_cc_setlinewid", "_cc_setgraphic", "_cc_set2dangle", "_cc_settiling", "_cc_setmodel", "_cc_setmodelangle", "_cc_setmodelanim", "_cc_setmodelorthog", "_cc_setmodeltint", "_cc_setmodellighting", "_cc_resetmodellighting", "_cc_settext", "_cc_settextfont", "_cc_settextalign", "_cc_settextshadow", "_cc_settextantimacro", "_cc_setoutline", "_cc_setgraphicshadow", "_cc_setvflip", "_cc_sethflip", "_cc_setscrollsize", "_cc_setalpha", "_cc_setmodelzoom", "_cc_setlinedirection", "_cc_setmodelorigin", "_cc_setmaxlines", "_cc_setparam_int", "_cc_setparam_string", "_cc_setrecol", "_cc_setretex", "_cc_setfontmono", "_cc_setparam", "_cc_setclickmask", "_cc_setobject", "_cc_setnpchead", "_cc_setplayerhead_self", "_cc_setnpcmodel", "_cc_setplayermodel", "_cc_setobject_nonum", "_cc_setlinkfriend", "_cc_setlinkfriendchat", "_cc_setlinkplayergroup", "_cc_setlinkactiveclanchannel", "_cc_resetlinkplayer", "_cc_setobject_wearcol", "_cc_setobject_wearcol_nonum", "_cc_setplayermodel_self", "_cc_setobject_alwaysnum", "_cc_setobject_wearcol_alwaysnum", "_cc_setop", "_cc_setdraggable", "_cc_setdragrenderbehaviour", "_cc_setdragdeadzone", "_cc_setdragdeadtime", "_cc_setopbase", "_cc_settargetverb", "_cc_clearops", "_cc_settargetcursors", "_cc_setopcursor", "_cc_setpausetext", "_cc_settargetopcursor", "_cc_setopchar", "_cc_setoptchar", "_cc_setopkey", "_cc_setoptkey", "_cc_setopkeyrate", "_cc_setoptkeyrate", "_cc_setopkeyignoreheld", "_cc_setoptkeyignoreheld", "_cc_setmouseovercursor", "_cc_setonclick", "_cc_setonhold", "_cc_setonrelease", "_cc_setonmouseover", "_cc_setonmouseleave", "_cc_setondrag", "_cc_setontargetleave", "_cc_setonvartransmit", "_cc_setontimer", "_cc_setonop", "_cc_setondragcomplete", "_cc_setonclickrepeat", "_cc_setonmouserepeat", "_cc_setoninvtransmit", "_cc_setonstattransmit", "_cc_setontargetenter", "_cc_setonscrollwheel", "_cc_setonchattransmit", "_cc_setonkey", "_cc_setongamepadbutton", "_cc_setongamepadbuttonheld", "_cc_setongamepadaxis", "_cc_setongamepadtrigger", "_cc_setonfriendtransmit", "_cc_setonclantransmit", "_cc_setonmisctransmit", "_cc_setondialogabort", "_cc_setonsubchange", "_cc_setonstocktransmit", "_cc_setoncamfinished", "_cc_setonresize", "_cc_setonvarctransmit", "_cc_setonvarcstrtransmit", "_cc_setonopt", "_cc_setonclansettingstransmit", "_cc_setonclanchanneltransmit", "_cc_setonvarclantransmit", "_cc_setonplayergrouptransmit", "_cc_setonplayergroupvarptransmit", "_cc_setoncameraupdatetransmit", "_cc_clearscripthooks", "_cc_callonresize", "_if_setposition", "_if_setsize", "_if_sethide", "_if_setaspect", "_if_setnoclickthrough", "_if_setscrollpos", "_if_setcolour", "_if_setfill", "_if_settrans", "_if_setlinewid", "_if_setgraphic", "_if_set2dangle", "_if_settiling", "_if_setmodel", "_if_setmodelangle", "_if_setmodelanim", "_if_setmodelorthog", "_if_setmodeltint", "_if_setmodellighting", "_if_resetmodellighting", "_if_settext", "_if_settextfont", "_if_settextalign", "_if_settextshadow", "_if_settextantimacro", "_if_setoutline", "_if_setgraphicshadow", "_if_setvflip", "_if_sethflip", "_if_setscrollsize", "_if_setalpha", "_if_setmodelzoom", "_if_setlinedirection", "_if_setmodelorigin", "_if_setmaxlines", "_if_setparam_int", "_if_setparam_string", "_if_setrecol", "_if_setretex", "_if_setfontmono", "_if_setclickmask", "_if_setobject", "_if_setnpchead", "_if_setplayerhead_self", "_if_setnpcmodel", "_if_setplayermodel", "_if_setobject_nonum", "_if_setlinkfriend", "_if_setlinkfriendchat", "_if_setlinkplayergroup", "_if_setlinkactiveclanchannel", "_if_resetlinkplayer", "_if_setobject_wearcol", "_if_setobject_wearcol_nonum", "_if_setplayermodel_self", "_if_setobject_alwaysnum", "_if_setobject_wearcol_alwaysnum", "_if_setop", "_if_setdraggable", "_if_setdragrenderbehaviour", "_if_setdragdeadzone", "_if_setdragdeadtime", "_if_setopbase", "_if_settargetverb", "_if_clearops", "_if_settargetcursors", "_if_setopcursor", "_if_setpausetext", "_if_settargetopcursor", "_if_setopchar", "_if_setoptchar", "_if_setopkey", "_if_setoptkey", "_if_setopkeyrate", "_if_setoptkeyrate", "_if_setopkeyignoreheld", "_if_setoptkeyignoreheld", "_if_setmouseovercursor", "_if_setonclick", "_if_setonhold", "_if_setonrelease", "_if_setonmouseover", "_if_setonmouseleave", "_if_setondrag", "_if_setontargetleave", "_if_setonvartransmit", "_if_setontimer", "_if_setonop", "_if_setondragcomplete", "_if_setonclickrepeat", "_if_setonmouserepeat", "_if_setoninvtransmit", "_if_setonstattransmit", "_if_setontargetenter", "_if_setonscrollwheel", "_if_setonchattransmit", "_if_setonkey", "_if_setongamepadbutton", "_if_setongamepadbuttonheld", "_if_setongamepadaxis", "_if_setongamepadtrigger", "_if_setonfriendtransmit", "_if_setonclantransmit", "_if_setonmisctransmit", "_if_setondialogabort", "_if_setonsubchange", "_if_setonstocktransmit", "_if_setoncamfinished", "_if_setonresize", "_if_setonvarctransmit", "_if_setonvarcstrtransmit", "_if_setonopt", "_if_setonclansettingstransmit", "_if_setonclanchanneltransmit", "_if_setonvarclantransmit", "_if_setonplayergrouptransmit", "_if_setonplayergroupvarptransmit", "_if_setoncameraupdatetransmit", "_if_clearscripthooks", "_if_callonresize", "_if_npc_setcustombodymodel", "_cc_npc_setcustombodymodel", "_if_npc_setcustomheadmodel", "_cc_npc_setcustomheadmodel", "_if_npc_setcustomrecol", "_cc_npc_setcustomrecol", "_if_npc_setcustomretex", "_cc_npc_setcustomretex", "_mes", "_return", "_if_close", "_resume_countdialog", "_resume_namedialog", "_resume_stringdialog", "_abort_dialog", "_opplayer", "_resume_objdialog", "_if_dragpickup", "_cc_dragpickup", "_if_opensubclient", "_if_closesubclient", "_opplayert", "_mes_typed", "_setup_messagebox", "_resume_hsldialog", "_resume_clanforumqfcdialog", "_sound_synth", "_sound_song", "_sound_jingle", "_sound_synth_volume", "_sound_song_volume", "_sound_jingle_volume", "_sound_vorbis_volume", "_sound_speech_volume", "_sound_synth_rate", "_sound_vorbis_rate", "_email_validation_submit_code", "_email_validation_change_address", "_email_validation_add_new_address", "_friend_setrank", "_friend_setnotes", "_friend_add", "_friend_del", "_ignore_add", "_ignore_del", "_ignore_setnotes", "_clan_kickuser", "_clan_joinchat", "_clan_leavechat", "_ignore_add_temp", "_affinedclansettings_addbanned_fromchannel", "_affinedclansettings_setmuted_fromchannel", "_activeclanchannel_kickuser", "_oc_findrestart", "_chat_setfilter", "_chat_sendabusereport", "_chat_setmode", "_chat_sendpublic", "_chat_sendprivate", "_activechatphrase_prepare", "_activechatphrase_send", "_activechatphrase_sendprivate", "_activechatphrase_setdynamicint", "_activechatphrase_setdynamicobj", "_chatphrase_findrestart", "_worldmap_3dview_enable", "_worldmap_3dview_disable", "_worldmap_3dview_setloddistance", "_worldmap_setcategorypriority", "_worldmap_3dview_settextfont", "_worldmap_disabletextsize", "_worldmap_disabletype", "_worldmap_3dview_setlighting", "_worldmap_setzoom", "_worldmap_setmap", "_worldmap_jumptosourcecoord", "_worldmap_jumptodisplaycoord", "_worldmap_setmap_coord", "_worldmap_flashelement", "_worldmap_setmap_coord_override", "_worldmap_disableelements", "_worldmap_flashelementcategory", "_worldmap_disableelementcategory", "_worldmap_disableelement", "_worldmap_closemap", "_fullscreen_exit", "_setwindowmode", "_setdefaultwindowmode", "_openurl", "_urlencode", "_spline_new", "_spline_addpoint", "_quit", "_openurl_nologin", "_writeconsole", "_formatminimenu", "_defaultminimenu", "_setdefaultcursors", "_sethardcodedopcursors", "_docheat", "_notify_accountcreated", "_notify_accountcreatestarted", "_setsubmenuminlength", "_openurl_shim", "_cam_moveto", "_cam_lookat", "_cam_movealong", "_cam_reset", "_cam_forceangle", "_cam_inc_x", "_cam_dec_x", "_cam_inc_y", "_cam_dec_y", "_cam_setfollowheight", "_cam_followcoord", "_cam_smoothreset", "_cam_removeroof", "_cam2_setlookatmode", "_cam2_setpositionmode", "_cam2_setlookatacceleration", "_cam2_setpositionacceleration", "_cam2_setlookatmaxspeed", "_cam2_setpositionmaxspeed", "_cam2_setdepthplanes", "_cam2_setfieldofview", "_cam2_setlookatpoint_point", "_cam2_setpositionpoint_point", "_cam2_getpositionpoint_point", "_cam2_setlookatentity_player", "_cam2_setlookatentity_npc", "_cam2_setpositionentity_player", "_cam2_setpositionentity_npc", "_cam2_setfieldofviewscreen", "_cam2_setpositionspline_spline", "_cam2_setlookatspline_spline", "_cam2_removeeffect", "_cam2_removealleffects", "_cam2_setlookatacceleration_axis", "_cam2_setpositionacceleration_axis", "_cam2_setlookatmaxspeed_axis", "_cam2_setpositionmaxspeed_axis", "_cam2_setcollisionmode", "_cam2_setpositionpointcollision", "_cam2_updateeffect_ztilt", "_cam2_settraildistance", "_cam2_setlinearmovementmode", "_cam2_setspringproperties", "_cam2_setlookatangularinterpolation", "_cam2_setpositionangularinterpolation", "_cam2_setlookatspringproperties", "_cam2_setpositionspringproperties", "_cam2_enable", "_cam2_setsnapdistances", "_cam2_resetsnapdistances", "_login_request", "_cam2_setlookatorientation_vector", "_cam2_setlookatorientation_xrotation", "_cam2_setlookatorientation_yrotation", "_cam2_setlookatorientation_xmovement", "_cam2_setlookatorientation_zmovement", "_cam2_setlookatorientation_maxdistanceclamping", "_login_continue", "_login_resetreply", "_resend_uid_passport_request", "_create_availablerequest", "_create_name_availablerequest", "_create_suggest_name_request", "_create_createrequest", "_create_connectrequest", "_create_step_reached", "_lobby_entergame", "_lobby_enterlobby", "_lobby_leavelobby", "_create_setunder13", "_login_cancel", "_login_request_social_network", "_lobby_enterlobby_social_network", "_detail_brightness", "_detail_removeroofs_option", "_detail_grounddecor_on", "_detail_idleanims_many", "_detail_flickering_on", "_detail_spotshadows_on", "_detail_hardshadows", "_detail_shadowquality", "_detail_lightdetail_high", "_detail_waterdetail_high", "_detail_fog_on", "_detail_antialiasing", "_detail_antialiasing_quality", "_detail_stereo", "_detail_soundvol", "_detail_musicvol", "_detail_bgsoundvol", "_detail_removeroofs_option_override", "_detail_particles", "_detail_antialiasing_default", "_detail_buildarea", "_detail_bloom", "_detail_customcursors", "_detail_idleanims", "_detail_groundblending", "_detail_toolkit", "_detail_toolkit_default", "_detail_cpuusage", "_detail_texturing", "_detail_maxscreensize", "_detail_speechvol", "_detail_loginvol", "_detail_loadingscreentype", "_detail_drawdistance", "_detail_skydetail", "_detail_animdetail", "_detail_diskcachesize", "_viewport_setfov", "_viewport_setzoom", "_viewport_clampfov", "_worldlist_sort", "_worldlist_autoworld", "_worldlist_pingworlds", "_if_debug_button1", "_if_debug_button2", "_if_debug_button3", "_if_debug_button4", "_if_debug_button5", "_if_debug_button6", "_if_debug_button7", "_if_debug_button8", "_if_debug_button9", "_if_debug_button10", "_if_debug_target", "_if_triggerop", "_cc_triggerop", "_force_interface_drag", "_cancel_interface_drag", "_targetmode_active", "_targetmode_cancel", "_autosetup_sethigh", "_autosetup_setmedium", "_autosetup_setlow", "_autosetup_setmin", "_autosetup_setcustom", "_autosetup_blackflaglast", "_video_advert_force_remove", "_video_advert_allow_skip", "_bug_report", "_array_sort", "_cc_find", "_if_find", "_cc_getx", "_cc_gety", "_cc_getwidth", "_cc_getheight", "_cc_gethide", "_cc_getlayer", "_cc_getparentlayer", "_cc_getcolour", "_cc_getscrollx", "_cc_getscrolly", "_cc_gettext", "_cc_getscrollwidth", "_cc_getscrollheight", "_cc_getmodelzoom", "_cc_getmodelangle_x", "_cc_getmodelangle_z", "_cc_getmodelangle_y", "_cc_gettrans", "_cc_getmodelxof", "_cc_getmodelyof", "_cc_getgraphic", "_cc_param", "_cc_get2dangle", "_cc_getmodel", "_cc_getfontgraphic", "_cc_getgraphicdimensions", "_cc_getfontmetrics", "_cc_getinvobject", "_cc_getinvcount", "_cc_getid", "_cc_gettargetmask", "_cc_getop", "_cc_getopbase", "_cc_getcharindexatpos", "_cc_getcharposatindex", "_if_getx", "_if_gety", "_if_getwidth", "_if_getheight", "_if_gethide", "_if_getlayer", "_if_getparentlayer", "_if_getcolour", "_if_getscrollx", "_if_getscrolly", "_if_gettext", "_if_getscrollwidth", "_if_getscrollheight", "_if_getmodelzoom", "_if_getmodelangle_x", "_if_getmodelangle_z", "_if_getmodelangle_y", "_if_gettrans", "_if_getmodelxof", "_if_getmodelyof", "_if_getgraphic", "_if_get2dangle", "_if_getmodel", "_if_getfontgraphic", "_if_getgraphicdimensions", "_if_getfontmetrics", "_if_getinvobject", "_if_getinvcount", "_if_hassub", "_if_getnextsubid", "_if_hassubmodal", "_if_hassuboverlay", "_if_gettargetmask", "_if_getop", "_if_getopbase", "_if_getcharindexatpos", "_if_getcharposatindex", "_clientclock", "_inv_getobj", "_inv_getnum", "_inv_total", "_inv_totalcat", "_inv_size", "_inv_stockbase", "_inv_getvar", "_stat", "_stat_base", "_stat_visible_xp", "_stat_base_actual", "_stat_visible_xp_actual", "_facing_fine", "_coord", "_coordx", "_coordy", "_coordz", "_coord_fine", "_coordx_fine", "_coordy_fine", "_coordz_fine", "_coordlevel_fine", "_movecoord_fine", "_coord_gridtofine", "_coord_finetogrid", "_map_members", "_map_preload", "_map_loadedpercent", "_invother_getobj", "_invother_getnum", "_invother_total", "_invother_getvar", "_staffmodlevel", "_reboottimer", "_map_world", "_runenergy_visible", "_runweight_visible", "_playermod", "_playermodlevel", "_playermember", "_comlevel_active", "_gender", "_map_quickchat", "_inv_freespace", "_inv_totalparam", "_inv_totalparam_stack", "_map_lang", "_movecoord", "_affiliate", "_profile_cpu", "_playerdemo", "_applet_hasfocus", "_frombilling", "_get_mousex", "_get_mousey", "_get_active_minimenu_entry", "_get_second_minimenu_entry", "_get_minimenu_length", "_npc_find_active_minimenu_entry", "_player_find_active_minimenu_entry", "_get_currentcursor", "_get_selfyangle", "_map_isowner", "_get_mousebuttons", "_self_player_uid", "_get_minimenu_target", "_enum_string", "_enum", "_enum_hasoutput", "_enum_hasoutput_string", "_enum_getoutputcount", "_enum_getreversecount", "_enum_getreversecount_string", "_enum_getreverseindex", "_enum_getreverseindex_string", "_friend_count", "_friend_getname", "_friend_getworld", "_friend_getrank", "_friend_getnotes", "_friend_getworldflags", "_friend_test", "_friend_getworldname", "_clan_getchatdisplayname", "_clan_getchatcount", "_clan_getchatusername", "_clan_getchatuserworld", "_clan_getchatuserrank", "_clan_getchatminkick", "_clan_getchatrank", "_ignore_count", "_ignore_getname", "_ignore_getnotes", "_ignore_test", "_clan_isself", "_clan_getchatownername", "_clan_getchatuserworldname", "_friend_platform", "_friend_getslotfromname", "_ignore_getslotfromname", "_playercountry", "_ignore_is_temp", "_clan_getchatusername_unfiltered", "_ignore_getname_unfiltered", "_friend_is_referrer", "_friend_is_referred", "_activeclansettings_find_listened", "_activeclansettings_find_affined", "_activeclansettings_getclanname", "_activeclansettings_getallowunaffined", "_activeclansettings_getranktalk", "_activeclansettings_getrankkick", "_activeclansettings_getranklootshare", "_activeclansettings_getcoinshare", "_activeclansettings_getaffinedcount", "_activeclansettings_getaffineddisplayname", "_activeclansettings_getaffinedrank", "_activeclansettings_getaffinedmuted", "_activeclansettings_getbannedcount", "_activeclansettings_getbanneddisplayname", "_activeclansettings_getaffinedextrainfo", "_activeclansettings_getcurrentowner_slot", "_activeclansettings_getreplacementowner_slot", "_activeclansettings_getaffinedslot", "_activeclansettings_getsortedaffinedslot", "_activeclansettings_getaffinedjoinruneday", "_activeclanchannel_find_listened", "_activeclanchannel_find_affined", "_activeclanchannel_getclanname", "_activeclanchannel_getrankkick", "_activeclanchannel_getranktalk", "_activeclanchannel_getusercount", "_activeclanchannel_getuserdisplayname", "_activeclanchannel_getuserrank", "_activeclanchannel_getuserworld", "_activeclanchannel_getuserslot", "_activeclanchannel_getsorteduserslot", "_clanprofile_find", "_stockmarket_getoffertype", "_stockmarket_getofferitem", "_stockmarket_getofferprice", "_stockmarket_getoffercount", "_stockmarket_getoffercompletedcount", "_stockmarket_getoffercompletedgold", "_stockmarket_isofferempty", "_stockmarket_isofferstable", "_stockmarket_isofferfinished", "_stockmarket_isofferadding", "_add", "_sub", "_multiply", "_divide", "_random", "_randominc", "_interpolate", "_addpercent", "_setbit", "_clearbit", "_testbit", "_modulo", "_pow", "_invpow", "_and", "_or", "_min", "_max", "_scale", "_random_sound_pitch", "_hsvtorgb", "_not", "_append_num", "_append", "_append_signnum", "_get_col_tag", "_lowercase", "_fromdate", "_text_gender", "_tostring", "_compare", "_paraheight", "_parawidth", "_paraline", "_text_switch", "_escape", "_append_char", "_char_isprintable", "_char_isalphanumeric", "_char_isalpha", "_char_isnumeric", "_string_length", "_substring", "_removetags", "_string_indexof_char", "_string_indexof_string", "_char_tolowercase", "_char_touppercase", "_tostring_localised", "_stringwidth", "_string_distance", "_format_datetime_from_minutes", "_clanforumqfc_tostring", "_oc_name", "_oc_op", "_oc_iop", "_oc_cost", "_oc_stackable", "_oc_category", "_oc_hasvarobj", "_oc_cert", "_oc_uncert", "_oc_shard", "_oc_unshard", "_oc_shardcount", "_oc_wearpos", "_oc_wearpos2", "_oc_wearpos3", "_oc_members", "_oc_param", "_oc_icursor", "_oc_multistacksize", "_oc_find", "_oc_findnext", "_oc_minimenu_colour_overridden", "_oc_minimenu_colour", "_nc_param", "_lc_param", "_struct_param", "_seq_param", "_seqlength", "_bas_getanim_ready", "_chat_getfilter_public", "_chat_gethistory_bytypeandline", "_chat_gethistory_byuid", "_chat_getfilter_private", "_chat_playername", "_chat_getfilter_trade", "_chat_gethistorylength", "_chat_getnextuid", "_chat_getprevuid", "_chat_playername_unfiltered", "_chatcat_getdesc", "_chatcat_getsubcatcount", "_chatcat_getsubcat", "_chatcat_getphrasecount", "_chatcat_getphrase", "_chatphrase_gettext", "_chatphrase_getautoresponsecount", "_chatphrase_getautoresponse", "_chatcat_getsubcatshortcut", "_chatcat_getphraseshortcut", "_chatcat_findsubcatbyshortcut", "_chatcat_findphrasebyshortcut", "_chatphrase_getdynamiccommandcount", "_chatphrase_getdynamiccommand", "_chatphrase_getdynamiccommandparam_enum", "_chatphrase_find", "_chatphrase_findnext", "_keyheld_alt", "_keyheld_ctrl", "_keyheld_shift", "_worldmap_3dview_active", "_worldmap_3dview_getcoordfine", "_worldmap_3dview_getloddistance", "_worldmap_getcategorypriority", "_worldmap_3dview_gettextfont", "_worldmap_3dview_getscreenposition", "_worldmap_getdisabletextsize", "_worldmap_getdisabletype", "_worldmap_getzoom", "_worldmap_getmap", "_worldmap_getmapname", "_worldmap_getsize", "_worldmap_getdisplayposition", "_worldmap_getconfigorigin", "_worldmap_getconfigsize", "_worldmap_getconfigbounds", "_worldmap_listelement_start", "_worldmap_listelement_next", "_worldmap_coordinmap", "_worldmap_getconfigzoom", "_worldmap_isloaded", "_worldmap_getsourceposition", "_worldmap_getdisplaycoord", "_worldmap_getsourcecoord", "_worldmap_getdisableelements", "_worldmap_getdisableelementcategory", "_worldmap_getdisableelement", "_worldmap_getcurrentmap", "_worldmap_findnearestelement", "_fullscreen_enter", "_fullscreen_modecount", "_fullscreen_getmode", "_fullscreen_lastmode", "_getwindowmode", "_getdefaultwindowmode", "_spline_length", "_lastlogin", "_minimenuopen", "_getclipboard", "_has_html5", "_has_nxt", "_clienttype", "_is_gamescreen_state", "_cam_getangle_xa", "_cam_getangle_ya", "_cam_getfollowheight", "_cam2_getcontrolmode", "_cam2_getlookatmode", "_cam2_getpositionmode", "_cam2_getpositionentity_angleoffsets", "_cam2_addeffect_shake", "_cam2_getpositionentity_lookatangleoffsets", "_cam2_getpositionentity_lookatangle", "_cam2_getpositionentity_lookatdistance", "_cam2_addeffect_ztilt", "_cam2_isenabled", "_cam2_legacycam_ready", "_cam_modeisfollowplayer", "_login_reply", "_login_hoptime", "_login_ban_duration", "_create_reply", "_create_email_validate_reply", "_create_name_validate_reply", "_create_suggest_name_reply", "_create_connect_reply", "_login_disallowresult", "_lobby_entergamereply", "_lobby_enterlobbyreply", "_userflowflags", "_automatedtestflags", "_create_under13", "_login_last_transfer_reply", "_login_inprogress", "_login_queue_position", "_login_disallowtrigger", "_create_get_email", "_login_accountappeal", "_detailget_brightness", "_detailget_removeroofs_option", "_detailget_grounddecor_on", "_detailget_idleanims_many", "_detailget_flickering_on", "_detailget_spotshadows_on", "_detailget_hardshadows", "_detailget_shadowquality", "_detailget_lightdetail_high", "_detailget_waterdetail_high", "_detailget_fog_on", "_detailget_antialiasing", "_detailget_antialiasing_quality", "_detailget_stereo", "_detailget_soundvol", "_detailget_musicvol", "_detailget_bgsoundvol", "_detailget_particles", "_detailget_antialiasing_default", "_detailget_buildarea", "_detailget_bloom", "_detailget_customcursors", "_detailget_idleanims", "_detailget_groundblending", "_detailget_toolkit", "_detailget_toolkit_default", "_detailget_cpuusage", "_detailget_texturing", "_detailget_performance_metric", "_detailget_maxscreensize", "_detailget_speechvol", "_detailget_loginvol", "_detailget_safemode", "_detailget_loadingscreentype", "_detailget_orthographic", "_detailget_canchoosesafemode", "_detailget_chosesafemode", "_detailget_skydetail", "_detailget_drawdistance", "_detailget_maxdiskcachesize", "_detailget_mindiskcachesize", "_detailget_recommendeddiskcachesize", "_detailget_diskcachesize", "_detailget_animdetail", "_viewport_geteffectivesize", "_viewport_getzoom", "_viewport_getfov", "_date_minutes", "_date_runeday", "_date_runeday_fromdate", "_date_year", "_date_isleapyear", "_date_runeday_todate", "_date_minutes_fromruneday", "_worldlist_fetch", "_worldlist_start", "_worldlist_next", "_worldlist_switch", "_worldlist_specific", "_worldlist_specific_thisworld", "_if_gettop", "_if_debug_getopenifcount", "_if_debug_getopenifid", "_if_debug_getname", "_if_debug_getcomcount", "_if_debug_getcomname", "_if_debug_getservertriggers", "_opcount", "_mec_text", "_mec_sprite", "_mec_textsize", "_mec_category", "_mec_param", "_userdetail_quickchat", "_userdetail_lobby_membership", "_userdetail_lobby_recoveryday", "_userdetail_lobby_unreadmessages", "_userdetail_lobby_lastloginday", "_userdetail_lobby_lastloginaddress", "_userdetail_lobby_emailstatus", "_userdetail_lobby_ccexpiry", "_userdetail_lobby_graceexpiry", "_userdetail_lobby_dobrequested", "_userdetail_dob", "_userdetail_lobby_membersstats", "_userdetail_lobby_playage", "_userdetail_lobby_jcoins_balance", "_userdetail_lobby_loyalty_balance", "_autosetup_dosetup", "_autosetup_getlevel", "_video_advert_play", "_video_advert_has_finished", "_get_entity_say", "_get_displayname_withextras", "_get_npc_name", "_get_npc_vislevel", "_get_entity_screen_position", "_if_get_gamescreen", "_if_set_gamescreen_enabled", "_get_entity_overlay_height", "_get_npc_stat", "_is_npc_active", "_is_npc_visible", "_is_targeted_entity", "_npc_type", "_get_loc_screen_position", "_get_obj_screen_position", "_get_loc_overlay_height", "_get_obj_overlay_height", "_get_loc_bounding_box", "_get_obj_bounding_box", "_get_entity_bounding_box", "_quest_getname", "_quest_getsortname", "_quest_type", "_quest_getdifficulty", "_quest_getmembers", "_quest_points", "_quest_questreq_count", "_quest_questreq", "_quest_questreq_met", "_quest_pointsreq", "_quest_pointsreq_met", "_quest_statreq_count", "_quest_statreq_stat", "_quest_statreq_level", "_quest_statreq_met", "_quest_varpreq_count", "_quest_varpreq_desc", "_quest_varpreq_met", "_quest_varbitreq_count", "_quest_varbitreq_desc", "_quest_varbitreq_met", "_quest_allreqmet", "_quest_started", "_quest_finished", "_quest_param", "_map_build_complete", "_sound_song_stop", "_sound_group_start", "_sound_group_stop", "_sound_vorbis_volume_rate_group", "_sound_mixbuss_add", "_sound_mixbuss_setlevel", "_sound_distancefocusfilter_setparams", "_preload_percent", "_shader_preload_allow", "_shader_preload_percent", "_shader_preload_throttle", "_can_run_java_client", "_fps_stats", "_runjavascript", "_worldmap_jumptosourcecoord_instant", "_worldmap_jumptodisplaycoord_instant", "_worldmap_setflashloops", "_worldmap_setflashloops_default", "_worldmap_setflashtics", "_worldmap_setflashtics_default", "_worldmap_perpetualflash", "_worldmap_stopcurrentflashes", "_player_group_find", "_player_group_member_count", "_player_group_member_get_same_world_var", "_player_group_member_get_rank", "_player_group_member_get_team", "_player_group_member_get_last_seen_node_id", "_player_group_member_get_status", "_player_group_member_is_online", "_player_group_member_is_member", "_player_group_member_get_displayname", "_player_group_member_get_join_xp", "_player_group_member_is_owner", "_player_group_banned_count", "_player_group_banned_get_displayname", "_player_group_get_displayname", "_player_group_get_max_size", "_player_group_get_create_mins_since_epoch", "_player_group_get_create_seconds_to_now", "_player_group_is_members_only", "_player_group_get_overall_status", "_player_group_get_owner_slot", "_ttv_login", "_ttv_login_getstate", "_ttv_logout", "_ttv_library_getstate", "_ttv_library_request", "_ttv_stream_getstate", "_ttv_stream_start", "_ttv_stream_stop", "_ttv_stream_settitle", "_ttv_stream_getviewers", "_ttv_stream_getquality", "_ttv_stream_setsmoothresize", "_ttv_chat_getstate", "_ttv_chat_sendmessage", "_ttv_webcam_getstate", "_ttv_webcam_supported", "_ttv_webcam_getdevice_count", "_ttv_webcam_getdevice_byindex", "_ttv_webcam_getdevice_byuniquename", "_ttv_webcam_getcap_count", "_ttv_webcam_getcap_byindex", "_ttv_webcam_getcap_byuniqueid", "_ttv_webcam_start", "_ttv_webcam_stop", "_ttv_webcam_flip", "_ttv_livestreams_update", "_ttv_livestreams_getstream_start", "_ttv_livestreams_getstream_next", "_ttv_setdebugouput", "_ttv_hasprerequisites", "_os_ismac", "_os_iswindows", "_os_islinux", "_os_physicalmemorysize", "_getgridcoordrelativetocamera", "_movescripted", "_telemetry_get_group_count", "_telemetry_get_group_index", "_telemetry_get_group_id", "_telemetry_get_row_count", "_telemetry_get_row_index", "_telemetry_get_row_id", "_telemetry_is_row_pinned", "_telemetry_get_column_count", "_telemetry_get_column_index", "_telemetry_get_column_id", "_telemetry_get_grid_value", "_telemetry_is_grid_processor_set", "_emoji_add", "_emoji_remove", "_emoji_removeall", "_emoji_substitute", "_emoji_enable_auto_chatline", "_db_find", "_db_find_with_count", "_db_findnext", "_db_getfield", "_db_getfieldcount", "_detailcanmod_grounddecor", "_detailcanmod_spotshadows", "_detailcanmod_hardshadows", "_detailcanmod_shadowquality", "_detailcanmod_waterdetail", "_detailcanmod_antialiasing", "_detailcanmod_antialiasing_quality", "_detailcanmod_particles", "_detailcanmod_buildarea", "_detailcanmod_bloom", "_detailcanmod_groundblending", "_detailcanmod_texturing", "_detailcanmod_maxscreensize", "_detailcanmod_fog", "_detailcanmod_orthographic", "_detailcanmod_toolkit_default", "_detailcanmod_skydetail", "_detailcanmod_animdetail", "_detailcanset_grounddecor", "_detailcanset_spotshadows", "_detailcanset_hardshadows", "_detailcanset_shadowquality", "_detailcanset_waterdetail", "_detailcanset_antialiasing", "_detailcanset_antialiasing_quality", "_detailcanset_particles", "_detailcanset_buildarea", "_detailcanset_bloom", "_detailcanset_groundblending", "_detailcanset_texturing", "_detailcanset_maxscreensize", "_detailcanset_fog", "_detailcanset_orthographic", "_detailcanset_toolkit_default", "_detailcanset_skydetail", "_detailcanset_animdetail", "_detail_shadows", "_detail_lightingquality", "_detail_antialiasingmode", "_detail_ambientocclusion", "_detail_reflections", "_detail_vsync", "_detailget_shadows", "_detailget_lightingquality", "_detailget_antialiasingmode", "_detailget_ambientocclusion", "_detailget_reflections", "_detailget_vsync", "_detailcanmod_shadows", "_detailcanmod_lightingquality", "_detailcanmod_antialiasingmode", "_detailcanmod_ambientocclusion", "_detailcanmod_reflections", "_detailcanmod_vsync", "_detailcanset_shadows", "_detailcanset_lightingquality", "_detailcanset_antialiasingmode", "_detailcanset_ambientocclusion", "_detailcanset_reflections", "_detailcanset_vsync", "_autosetup_setultra", };
		names = new String[nameso.length];
		for (int i = 0; i < order.length; i++)
			names[order[i]] = nameso[i].replace(" ", "_");
	}

	public static void main(String[] args) throws Throwable {
	  	BufferedReader reader = new BufferedReader(new FileReader(new File("instr_read.txt")));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("instr_readx.txt")));
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File("db_missing.txt")));
		InstructionsDatabase db = new InstructionsDatabase(new File("instructions_db.ini"));
		

		
		
		
		
		int errcount = 0;
		List<String> normal = new ArrayList<String>();
		List<String> delegate = new ArrayList<String>();
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			String[] spl = line.split("\\:");
			int opcode = Integer.parseInt(spl[0].substring(1, spl[0].length() - 1));	
			if (db.getByScrampled(opcode) != null)
				continue;
			
			if (spl[1].startsWith("N/A ")) {
				System.out.println("Error on opcode:" + opcode + "; name=" + names[opcode]);
				errcount++;
				continue;
			}
			
			String[] data = spl[1].split("\\,");
			if (Integer.parseInt(data[0]) < 1000)
				normal.add(names[opcode] + " " + opcode + " " + (opcode + 10000) + " false\r\n");
			else
				delegate.add(names[opcode] + " " + opcode + " " + (opcode + 20000) + " false\r\n");
			
			writer.write(line + "\r\n");
		}
		if (errcount > 0)
			System.out.println("Errors:" + errcount);
		
		for (String str : normal)
			writer2.append(str);
		for (String str : delegate)
			writer2.append(str);
		
		
		reader.close();
		writer.close();
		writer2.close();
		
		//generateOpcodesDB();
		generateScriptsDB();
	}
	

	public static void generateScriptsDB() throws Throwable {
		InstructionsDatabase insDatabase = new InstructionsDatabase(new File("instructions_db.ini"));
		ConfigsDatabase cfgDatabase      = new ConfigsDatabase(new File("configs_db.ini"), new File("bitconfigs_db.ini"));
		FunctionDatabase opcodesDatabase = new FunctionDatabase(new File("opcodes_db.ini"));
		FunctionDatabase scriptsDatabase = new FunctionDatabase();
		CS2Decompiler dec = new CS2Decompiler(insDatabase, cfgDatabase, opcodesDatabase, scriptsDatabase, new TestProvider());
		//dec.setFlags(false, false, false);
		
		int decompiledScripts = 0;
		int lastDecompiledScripts = 0;
		int errors = 0;
		int notexisting = 0;
		boolean[] decompiled = new boolean[12346];
		do {
			lastDecompiledScripts = decompiledScripts;
			decompiledScripts = 0;
			errors = 0;
			notexisting = 0;
			
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("scripts_db.ini")));
			for (int i = 0; i < decompiled.length; i++) {
				if (!new File("scripts/" + i).exists()) {
					decompiled[i] = true;
					notexisting++;
					continue;
				}
				System.out.println("Script:" + i);
				
				if (!decompiled[i]) {
					FunctionNode func;
					try {
						func = dec.decompile(i);
					}
					catch (Exception ex) {
						ex.printStackTrace();
						errors++;
						continue;
					}
					if (func.getReturnType() != CS2Type.UNKNOWN) {
						writer.write(i + " " + func.getName() + " ");
						writer.write(func.getReturnType().toString());
						for (int a = 0; a < func.getArgumentLocals().length; a++)
							writer.write(" " + func.getArgumentLocals()[a].toString());
						writer.write("\r\n\r\n\r\n");
						decompiled[i] = true;
						decompiledScripts++;
					}
				}
				else {
					FunctionInfo info = scriptsDatabase.getInfo(i);
					writer.write(i + " " + info.getName() + " " + info.getReturnType().toString());
					for (int a = 0; a < info.getArgumentTypes().length; a++)
						writer.write(" " + info.getArgumentTypes()[a].toString() + " " + info.getArgumentNames()[a]);
					writer.write("\r\n\r\n\r\n");
					decompiledScripts++;
				}
			}
			writer.close();
			scriptsDatabase = new FunctionDatabase(new File("scripts_db.ini"));
			dec = new CS2Decompiler(insDatabase, cfgDatabase, opcodesDatabase, scriptsDatabase, new TestProvider());
			//dec.setFlags(false, false, false);
			
			
			System.err.println("Last:" + lastDecompiledScripts);
			System.err.println("Decompiled:" + decompiledScripts);
			System.err.println("Errors:" + errors);
			System.err.println("Not existing:" + notexisting);
		}
		while (decompiledScripts > lastDecompiledScripts);
	}
	
	
	public static void generateOpcodesDB() throws Throwable {
		BufferedReader reader = new BufferedReader(new FileReader(new File("instr_readx.txt")));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("opcodes_db.ini")));
		
		String[] strs = new String[names.length];

		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			if (line.length() <= 0 || line.startsWith(" ") || line.startsWith("//") || line.startsWith("#"))
				continue;
			String[] spl = line.split("\\:");
			String[] infospl = spl[1].split("\\,");
			int opcode = Integer.parseInt(spl[0].substring(1, spl[0].length() - 1));
			int originalOpcode = opcode;
			String name = names[opcode];
			int[] info = new int[infospl.length];
			for (int i = 0; i < info.length; i++)
				info[i] = Integer.parseInt(infospl[i]);
			opcode += info[0] >= 1000 ? 20000 : 10000;
			
			int ipush = info[0], spush = info[1], lpush = info[2], ipop = info[3], spop = info[4], lpop = info[5];
			
			if (opcode >= 20000) {
				ipush -= 2000;
				spush -= 2000;
				lpush -= 2000;
				ipop -= 2000;
				spop -= 2000;
				lpop -= 2000;
			}
			
			StringBuilder bld = new StringBuilder();
			bld.append(opcode + " ");
			bld.append(name + " ");
			if (ipush == -1 && spush == -1 && lpush == -1) {
				opcode = info[0] + 30000;
				bld.append("?? ");
			}
			else if (ipush == 0 && spush == 0 && lpush == 0)
				bld.append("void ");
			else if (ipush == 1 && spush == 0 && lpush == 0)
				bld.append("int ");
			else if (ipush == 0 && spush == 1 && lpush == 0)
				bld.append("string ");
			else if (ipush == 0 && spush == 0 && lpush == 1)
				bld.append("long ");
			else
				bld.append(CS2Type.forDesc(name + "_struct" + "(" + ipush + "," + spush + "," + lpush + ")").toString() + " ");
		
			int argsWrite = 0;
			for (int i = 0; i < ipop; i++)
				bld.append("int " + "arg" + argsWrite++ + " ");
			for (int i = 0; i < spop; i++)
				bld.append("string " + "arg" + argsWrite++ + " ");
			for (int i = 0; i < lpop; i++)
				bld.append("long " + "arg" + argsWrite++ + " ");
			
			String desc = bld.toString();
			if (desc.charAt(desc.length() - 1) == ' ')
				desc = desc.substring(0,desc.length() - 1);
			
			strs[originalOpcode] = desc + "\r\n\r\n\r\n";
		}
		
		for (int i = 0; i < order.length; i++) {
			if (strs[order[i]] != null)
				writer.write(strs[order[i]]);
		}
		
		reader.close();
		writer.close();
	}

}
