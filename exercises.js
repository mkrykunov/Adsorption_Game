function CheckAnswer1() {
   if (document.forms[0].group1[0].checked)
      alert("Correct");
   else if (document.forms[0].group1[1].checked)
      alert("Incorrect. Find out which molecular orbital is antisymmetric with respect to the mirror plane between two atoms");
}

function CheckAnswer2() {
   if (document.forms[2].group2[0].checked)
      alert("Correct");
   else if (document.forms[2].group2[1].checked)
      alert("Incorrect. Make sure that you are using a correct irreducible representation for this orbital");
}

function CheckAnswer3() {
   if (document.forms[3].group3[0].checked)
      alert("Correct");
   else if (document.forms[3].group3[1].checked)
      alert("Incorrect. Make sure that you have included the irrep of x-component for the dipole moment");
}

function CheckAnswer4() {
   if (document.forms[5].group4[0].checked)
      alert("Incorrect. Count the number of electrons promoted to the vacant orbitals");
   else if (document.forms[5].group4[1].checked)
      alert("Correct");
}

function ClearC2hTable() {
   var tableObj = document.getElementById("C2h_table");

   var selectedRow = tableObj.rows[5];

   for (var i = 1; i < selectedRow.cells.length; i++) {
      var selectedCell = selectedRow.cells[i];

      selectedCell.innerHTML = "";
   }
}

function EnterConfig (tableID) {
   var table = document.getElementById(tableID);
   var rowCount = table.rows.length;

   if (rowCount == 16) {
      alert("All configurations have been entered");
      return;
   }

   var newConfig = new Array("0", "0", "0", "0", "0", "0");

   var count = 0;

   for (var i = 0; i < document.forms[4].option2.length; i++) {
      var occupation = (document.forms[4].option2[i].checked);
      if (occupation) {
         newConfig[i] = "1";
         count++;
      }
   }

   if (count != 2) {
      alert("Please, select only two occupied orbitals");
      return;
   }

   for (var i = 1; i < rowCount; i++) {
      var row  = table.rows[i];
      var oldConfig = new Array("0", "0", "0", "0", "0", "0");

      count = 0;

      for (var j = 1; j < row.cells.length; j++) {
         var n_occ = row.cells[j].innerHTML;
         if (n_occ == "1") oldConfig[j - 1] = row.cells[j].innerHTML;
         if (oldConfig[j - 1] == newConfig[j - 1]) count++;
      }

      if (count == 6) {
         alert("This configuration has already been added");
         return;
      }
   }

   var newRow = table.insertRow(rowCount);

   var cell_0 = newRow.insertCell(0);
   cell_0.innerHTML = rowCount;

   for (var i = 0; i < document.forms[4].option2.length; i++) {
      var cell_i = newRow.insertCell(i + 1);
      var occupation = (document.forms[4].option2[i].checked);
      if (occupation)
         cell_i.innerHTML = 1;
      else
         cell_i.innerHTML = "";
   }

   if (rowCount == 15) alert("You have entered all configurations!");
}

function MultiplyIrreps() {
   var Ag = (document.forms[1].option1[0].checked);

   var Bg = (document.forms[1].option1[1].checked);

   var Au = (document.forms[1].option1[2].checked);

   var Bu = (document.forms[1].option1[3].checked);

   if (!(Ag || Bg || Au || Bu)) {
      alert("Please, select the irreducible representation");
      return;
   }

   var tableObj = document.getElementById("C2h_table");

   var selectedRow = tableObj.rows[5];

   var Ag_irrep = new Array("1", "1", "1", "1");
   var Bg_irrep = new Array("1", "-1", "1", "-1");
   var Au_irrep = new Array("1", "1", "-1", "-1");
   var Bu_irrep = new Array("1", "-1", "-1", "1");

   for (var i = 1; i < selectedRow.cells.length; i++) {
      var selectedCell = selectedRow.cells[i];

      var result = 1;

      if (Ag) result = result * Ag_irrep[i-1];
      if (Bg) result = result * Bg_irrep[i-1];
      if (Au) result = result * Au_irrep[i-1];
      if (Bu) result = result * Bu_irrep[i-1];

      if (!(Ag || Bg || Au || Bu)) result = 0;

      selectedCell.innerHTML = result;
   }
}

